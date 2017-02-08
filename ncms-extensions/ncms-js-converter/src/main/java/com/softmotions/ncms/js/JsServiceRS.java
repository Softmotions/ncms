package com.softmotions.ncms.js;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Striped;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.LightweightMessageFormatter;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.softmotions.commons.ThreadUtils;
import com.softmotions.commons.cont.CollectionUtils;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.io.DirUtils;
import com.softmotions.commons.lifecycle.Dispose;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.atm.ServerMessageEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.ncms.media.events.MediaDeleteEvent;
import com.softmotions.ncms.media.events.MediaUpdateEvent;
import com.softmotions.ncms.utils.Digest;
import com.softmotions.weboot.executor.TaskExecutor;
import com.softmotions.weboot.mb.MBDAOSupport;
import com.softmotions.weboot.scheduler.Scheduled;

/**
 * JS compiler service.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@Path("x/js")
public class JsServiceRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(JsServiceRS.class);

    private static final Pattern HASH_REGEXP = Pattern.compile("^[0-9a-f]{32}(\\.js)?$");

    private static final Striped<ReadWriteLock> RW_STRIPES = Striped.lazyWeakReadWriteLock(256);

    private final File jsCache;

    private final MediaReader mediaReader;

    private final NcmsEnvironment env;

    private final NcmsEventBus ebus;

    private final Map<String, ScriptSlot> activeScripts = new ConcurrentHashMap<>();

    private final TaskExecutor executor;

    boolean testingMode;


    @Inject
    public JsServiceRS(SqlSession sess,
                       MediaReader mediaReader,
                       NcmsEnvironment env,
                       TaskExecutor executor,
                       NcmsEventBus ebus) throws IOException {
        super(JsServiceRS.class.getName(), sess);
        this.mediaReader = mediaReader;
        this.env = env;
        this.executor = executor;
        this.ebus = ebus;
        this.jsCache = new File(mediaReader.getBaseDir(), ".jscache");
        DirUtils.ensureDir(jsCache, true);
    }

    /**
     * Retrieve compiled script by its fingerprint
     */
    @GET
    @Path("script/{fingerprint}")
    @Produces("application/javascript;charset=UTF-8")
    public String script(@PathParam("fingerprint") String fp) throws Exception {
        Matcher m = HASH_REGEXP.matcher(fp);
        String fname = fp;
        if (!m.matches()) {
            throw new BadRequestException();
        }
        if (m.group(1) == null) {
            fname += ".js";
        } else {
            fp = fp.substring(0, fp.length() - ".js".length());
        }
        File jsFile = new File(jsCache, fname);
        String data = null;
        ReadWriteLock rwlock = RW_STRIPES.get(fp);
        Lock lock = rwlock.readLock();
        lock.lock();
        try {
            if (jsFile.exists()) {
                data = FileUtils.readFileToString(jsFile, "utf-8");
            }
        } finally {
            lock.unlock();
        }
        if (data == null) {
            lock = rwlock.writeLock();
            lock.lock();
            try {
                if (jsFile.exists()) {
                    data = FileUtils.readFileToString(jsFile, "utf-8");
                } else {
                    data = recompileScript(fp);
                }
            } finally {
                lock.unlock();
            }
        }
        if (data == null) {
            throw new NotFoundException();
        }
        touchScript(fp);
        return data;
    }

    @GuardedBy("RW_STRIPES")
    @Nullable
    String recompileScript(String fp) throws Exception {
        String spec = selectOne("selectScriptSpec", fp);
        return spec != null ? compileScript(fp, new KVOptions(spec)) : null;
    }

    @GuardedBy("RW_STRIPES")
    @Nullable
    String compileScript(String fp, Collection<MediaResource> resources, KVOptions spec) throws Exception {

        List<SourceFile> inputs = new ArrayList<>(resources.size());
        for (MediaResource meta : resources) {
            inputs.add(SourceFile.fromCode(meta.getName(), meta.getSource()));
        }

        CompilerOptions options = new CompilerOptions();
        Compiler compiler = new Compiler(new ClosureLoggerErrorManager(log));
        options.setOutputCharset(Charset.forName("utf-8"));
        options.setLanguageIn(languageLevel2Closure(spec.getOrDefault("in", ALLOWED_JSGEN_OPTS.get("in"))));
        options.setLanguageOut(languageLevel2Closure(spec.getOrDefault("out", ALLOWED_JSGEN_OPTS.get("out"))));
        applyCompilationLevel(spec.getOrDefault("level", ALLOWED_JSGEN_OPTS.get("level")), options);

        List<String> inputPaths = resources.stream().map(MediaResource::getName).collect(Collectors.toList());
        log.info("Compiling script {}.js from: {} spec: {}",
                 fp,
                 inputPaths,
                 spec);

        Result result = compiler.compile(getExterns(), inputs, options);
        if (!result.success) {
            log.warn("Failed to compile script {}.js from: {} spec: {} opts: {}",
                     fp,
                     inputPaths,
                     spec,
                     (log.isDebugEnabled() ? options : ""));
            return null;
        }

        String data = compiler.toSource();
        FileUtils.write(new File(jsCache, fp + ".js"), data, "utf-8", false);
        return data;
    }


    @Nullable
    String compileScript(String fp, KVOptions spec) throws Exception {

        Long[] ids = Arrays.stream(spec.getOrDefault("scripts", "")
                                       .split(",")).map(Long::parseLong)
                           .toArray(Long[]::new);
        Set<MediaResource> resources = new LinkedHashSet<>(ids.length);
        for (int i = 0; i < ids.length; ++i) {
            MediaResource meta = mediaReader.findMediaResource(ids[i], null);
            if (meta == null) {
                String path = selectOne("selectScriptPathById", ids[i]);
                if (path == null) {
                    path = ids[i].toString();
                }
                log.error("Missing javascript file: {} fp: {} " +
                          "Script file will be compiled without this resource",
                          path, fp);
            } else {
                resources.add(meta);
            }
        }
        return compileScript(fp, resources, spec);
    }

    LanguageMode languageLevel2Closure(String in) {
        switch (in) {
            case "es3":
                return LanguageMode.ECMASCRIPT3;
            case "es6":
                return LanguageMode.ECMASCRIPT_2016;
            case "es5":
            default:
                return LanguageMode.ECMASCRIPT5;
        }
    }

    void applyCompilationLevel(String level, CompilerOptions options) {
        level = level.trim().toLowerCase();
        CompilationLevel cl;
        switch (level) {
            case "simple":
                cl = CompilationLevel.SIMPLE_OPTIMIZATIONS;
                break;
            case "advanced":
                cl = CompilationLevel.ADVANCED_OPTIMIZATIONS;
                break;
            case "none":
            default:
                cl = CompilationLevel.WHITESPACE_ONLY;
                break;
        }
        cl.setOptionsForCompilationLevel(options);
    }


    @SuppressWarnings("StaticCollection")
    private static final Map<String, String> ALLOWED_JSGEN_OPTS = new HashMap<String, String>() {{
        put("in", "es5");
        put("out", "es5");
        put("level", "simple");
    }};

    String computeFingerprint(String[] scripts, Map<String, String> _opts) {
        Map<String, String> opts = new HashMap<>(ALLOWED_JSGEN_OPTS.size());
        for (Map.Entry<String, String> de : ALLOWED_JSGEN_OPTS.entrySet()) {
            opts.put(de.getKey(), _opts.getOrDefault(de.getKey(), de.getValue()));
        }
        List<String> items = new ArrayList<>(scripts.length + opts.size());
        opts.entrySet().stream()
            .map(e -> e.getKey() + e.getValue())
            .collect(Collectors.toCollection(() -> items));
        Arrays.stream(scripts)
              .map(this::normalizePath)
              .collect(Collectors.toCollection(() -> items));
        Object[] itemsArr = items.toArray();
        Arrays.sort(itemsArr, Collator.getInstance());
        return Digest.getMD5(StringUtils.join(itemsArr));
    }

    String normalizePath(String path) {
        path = path.trim();
        if (!path.isEmpty() && path.charAt(0) != '/') {
            path = '/' + path;
        }
        return path;
    }

    String createScriptRef(String[] scripts, Map<String, String> opts) {
        String fp = computeFingerprint(scripts, opts);
        if (!activeScripts.containsKey(fp)) {
            ensureScript(fp, scripts, opts);
        } else {
            touchScript(fp);
        }
        return env.getAppRoot() + "/rs/x/js/script/" + fp + ".js";
    }

    @Transactional
    void ensureScript(String fp, String[] scripts, Map<String, String> opts) {

        String spec;
        ReadWriteLock rwlock = RW_STRIPES.get(fp);
        Lock rlock = rwlock.readLock();
        rlock.lock();
        try {
            spec = selectOne("selectScriptSpec", fp);
            if (spec != null) {
                touchScript(fp);
                return;
            }
        } finally {
            rlock.unlock();
        }

        // Collect info for script compilation
        KVOptions kvspec = new KVOptions();
        for (Map.Entry<String, String> de : ALLOWED_JSGEN_OPTS.entrySet()) {
            kvspec.put(de.getKey(), opts.getOrDefault(de.getKey(), de.getValue()));
        }
        StringBuilder files = new StringBuilder();
        Set<MediaResource> resources = new LinkedHashSet<>(scripts.length);
        for (int i = 0, c = 0; i < scripts.length; i++) {
            String path = scripts[i];
            path = normalizePath(path);
            MediaResource meta = mediaReader.findMediaResource(path, null);
            if (meta != null) {
                if (c++ > 0) {
                    files.append(',');
                }
                files.append(meta.getId().toString());
                resources.add(meta);
            }
        }
        kvspec.put("scripts", files.toString());

        // Get exclusive write lock
        Lock wlock = rwlock.writeLock();
        wlock.lock();
        try {
            spec = selectOne("selectScriptSpec", fp);
            if (spec != null) {
                // someone did our job already
                wlock.unlock();
                touchScript(fp);
                return;
            }

            if (compileScript(fp, resources, kvspec) == null) {
                // script failed to compile
                wlock.unlock();
                return;
            }

            // OK, then save dependencies
            for (MediaResource meta : resources) {
                insert("insertScriptDep",
                       "fingerprint", fp,
                       "id", meta.getId(),
                       "path", meta.getName());
            }

            // Save script spec for this fingerprint
            insert("insertScriptSpec",
                   "fingerprint", fp,
                   "spec", kvspec.toString());

            // Only if data persisted successfully
            ebus.doOnSuccessCommit(() -> touchScript(fp));
            // Unlock current write lock only when Transaction finished
            ebus.doOnTxFinish(wlock::unlock);
        } catch (Throwable e) {
            try {
                log.error("", e);
            } finally {
                // Unlock on error
                wlock.unlock();
            }
        }
    }

    List<SourceFile> getExterns() throws Exception {
        return CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER);
    }

    /**
     * Syntax checking of the specified JS file.
     * Return {@code true} of syntax is OK
     */
    boolean syntaxCheck(MediaUpdateEvent ev) {
        try {
            MediaResource meta = mediaReader.findMediaResource(ev.getId(), null);
            if (meta == null) {
                return false;
            }
            List<SourceFile> inputs = Collections.singletonList(SourceFile.fromCode(meta.getName(), meta.getSource()));
            CompilerOptions options = new CompilerOptions();
            ClosureLoggerErrorManager closureLogger = new ClosureLoggerErrorManager(log);
            Compiler compiler = new Compiler(closureLogger);
            options.setOutputCharset(Charset.forName("utf-8"));
            options.setLanguageIn(LanguageMode.ECMASCRIPT_2016);
            options.setLanguageOut(LanguageMode.ECMASCRIPT5);
            Result result = compiler.compile(getExterns(), inputs, options);
            if (!result.success) {
                LightweightMessageFormatter fmt = LightweightMessageFormatter.withoutSource();
                StringBuilder sb = new StringBuilder();
                JSError[] errors = closureLogger.getErrors();
                for (int i = 0; i < errors.length; i++) {
                    JSError err = errors[i];
                    if (i > 0) {
                        sb.append('\n');
                    }
                    sb.append(err.format(CheckLevel.ERROR, fmt));
                }
                reportError(sb.toString(), ev);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    void reportError(String msg, MediaUpdateEvent ev) {
        if (StringUtils.isBlank(msg)) {
            return;
        }
        ServerMessageEvent err = new ServerMessageEvent(this, msg, true, true, null);
        String app = (String) ev.hints().get("app");
        if (app != null) {
            err.hint("app", app);
            ebus.fire(err);
        } else if (testingMode) {
            ebus.fire(err);
        }
    }

    @Subscribe
    public void mediaUpdated(MediaUpdateEvent ev) {
        if (ev.isFolder() || !"js".equals(FilenameUtils.getExtension(ev.getPath()).toLowerCase())) {
            return;
        }
        executor.submit(() -> {
            ThreadUtils.cleanInheritableThreadLocals();
            try {
                if (syntaxCheck(ev)) {
                    updateJsFile(ev.getId());
                }
            } catch (Exception e) {
                log.error("", e);
            }
        });
    }

    public void mediaDeleted(MediaDeleteEvent ev) {
        if (ev.isFolder() || !"js".equals(FilenameUtils.getExtension(ev.getPath()).toLowerCase())) {
            return;
        }
        executor.submit(() -> {
            ThreadUtils.cleanInheritableThreadLocals();
            try {
                updateJsFile(ev.getId());
            } catch (Exception e) {
                log.error("", e);
            }
        });
    }

    @Transactional
    void updateJsFile(Long id) {
        log.info("Update JS file: {}", id);
        List<String> fps = select("selectAffectedFingerprints", id);
        // cleanup js cache
        for (String fp : fps) {
            ReadWriteLock rwlock = RW_STRIPES.get(fp);
            Lock lock = rwlock.writeLock();
            lock.lock();
            try {
                File f = new File(jsCache, fp + ".js");
                if (f.exists()) {
                    f.delete();
                }
            } catch (Throwable tr) {
                log.error("Unable to delete: {}", new File(jsCache, fp + ".js"), tr);
            } finally {
                activeScripts.remove(fp);
                lock.unlock();
            }
        }
    }

    @Start
    public void start() {
        ebus.register(this);
        cleanupOldScripts();
    }

    @Dispose
    public void shutdown() {
        ebus.unregister(this);
    }

    /**
     * Flush scripts access time into DB and
     * cleanup forgotten scripts
     * every 15 min
     */
    @Scheduled("*/15 * * * *")
    @Transactional
    public void cleanupOldScripts() {
        // Flush duty status in DB
        List<String> dirtyFps =
                activeScripts.entrySet().stream()
                             .filter(e -> {
                                 if (e.getValue().dirty) {
                                     e.getValue().dirty = false;
                                     return true;
                                 }
                                 return false;
                             })
                             .map(Map.Entry::getKey)
                             .collect(Collectors.toList());

        for (Collection<String> dg : CollectionUtils.split(dirtyFps, 128)) {
            update("touchScriptSpecs", dg);
        }

        // Cleanup old script specs and deps from DB
        int forgottenScriptLifetime =
                env.xcfg().getInt("media.js.forgotten-scripts-max-life-days", 30);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1 * forgottenScriptLifetime);
        Date date = cal.getTime();
        delete("deleteOldDeps", date);
        delete("deleteOldSpecs", date);

        // Leave old files in jscache untouched
    }

    void touchScript(String fp) {
        ScriptSlot slot = activeScripts.get(fp);
        if (slot == null) {
            activeScripts.put(fp, new ScriptSlot());
        } else {
            slot.touch();
        }
    }

    static class ScriptSlot {
        volatile boolean dirty;

        private ScriptSlot() {
            this.dirty = true;
        }

        void touch() {
            dirty = true;
        }

        void reset() {
            dirty = false;
        }
    }
}
