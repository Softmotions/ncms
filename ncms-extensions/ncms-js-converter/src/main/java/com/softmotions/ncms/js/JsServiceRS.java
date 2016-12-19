package com.softmotions.ncms.js;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Striped;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.io.DirUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.ncms.utils.Digest;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * JS compiler service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@Path("adm/rs/x/js")
public class JsServiceRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(JsServiceRS.class);

    private static final Pattern HASH_REGEXP = Pattern.compile("^[0-9a-f]{32}(\\.js)?$");

    private static final Striped<ReadWriteLock> RW_STRIPES = Striped.lazyWeakReadWriteLock(256);

    private final File jsCache;

    private final MediaReader mediaReader;

    private final NcmsEnvironment env;

    private final Set<String> activeFingerprints = ConcurrentHashMap.newKeySet();


    @Inject
    public JsServiceRS(SqlSession sess,
                       MediaReader mediaReader,
                       NcmsEnvironment env) throws IOException {
        super(JsServiceRS.class.getName(), sess);
        this.mediaReader = mediaReader;
        this.env = env;
        this.jsCache = new File(mediaReader.getBaseDir(), ".jscache");
        DirUtils.ensureDir(jsCache, true);
    }

    /**
     * Retrieve compiled script by its fingerprint
     */
    @Path("/script/{fingerprint}")
    @Produces("application/javascript;charset=UTF-8")
    public String script(@PathParam("fingerprint") String fp) throws Exception {
        Matcher m = HASH_REGEXP.matcher(fp);
        if (!m.matches()) {
            throw new BadRequestException();
        }
        if (m.group(1) == null) {
            fp += ".js";
        }
        File jsFile = new File(jsCache, fp);
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
    String compileScript(String fp, Set<MediaResource> resources, KVOptions spec) throws Exception {

        List<SourceFile> externs = CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER);
        List<SourceFile> inputs = new ArrayList<>(resources.size());
        for (MediaResource meta : resources) {
            inputs.add(SourceFile.fromCode(meta.getName(), meta.getSource()));
        }

        CompilerOptions options = new CompilerOptions();
        Compiler compiler = new Compiler(new ClosureLoggerErrorManager(log));
        options.setOutputCharset(Charset.forName("utf-8"));
        options.setLanguageIn(languageLevel2Closure(spec.getOrDefault("in", ALLOWED_JSGEN_OPTS.get("in"))));
        options.setLanguageOut(languageLevel2Closure(spec.getOrDefault("out", ALLOWED_JSGEN_OPTS.get("out"))));

        Result result = compiler.compile(externs, inputs, options);
        if (!result.success) {
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
        Set<MediaResource> resources = new HashSet<>(ids.length);
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
                return LanguageMode.ECMASCRIPT_2015;
            case "es5":
            default:
                return LanguageMode.ECMASCRIPT5;
        }
    }

    @SuppressWarnings("StaticCollection")
    private static final Map<String, String> ALLOWED_JSGEN_OPTS = new HashMap<String, String>() {{
        put("in", "es5");
        put("out", "es5");
    }};

    String computeFingerprint(Collection<String> scripts, Map<String, String> _opts) {
        Map<String, String> opts = new HashMap<>(ALLOWED_JSGEN_OPTS.size());
        for (Map.Entry<String, String> de : ALLOWED_JSGEN_OPTS.entrySet()) {
            opts.put(de.getKey(), _opts.getOrDefault(de.getKey(), de.getValue()));
        }
        List<String> items = new ArrayList<>(scripts.size() + opts.size());
        opts.entrySet().stream()
            .map(e -> e.getKey() + e.getValue())
            .collect(Collectors.toCollection(() -> items));
        scripts.stream()
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

    public String createScriptRef(List<String> scripts, Map<String, String> opts) {
        String fp = computeFingerprint(scripts, opts);
        if (!activeFingerprints.contains(fp)) {
            ensureScript(fp, scripts, opts);
        }
        return env.getAppRoot() + "/rs/x/js/" + fp;
    }

    @Transactional
    public void ensureScript(String fp, List<String> scripts, Map<String, String> opts) {
        String spec = selectOne("selectScriptSpec", fp);
        if (spec != null) {
            activeFingerprints.add(fp);
            return;
        }
        KVOptions kvspec = new KVOptions();
        for (Map.Entry<String, String> de : ALLOWED_JSGEN_OPTS.entrySet()) {
            kvspec.put(de.getKey(), opts.getOrDefault(de.getKey(), de.getValue()));
        }
        StringBuilder files = new StringBuilder();
        Set<MediaResource> resources = new HashSet<>(scripts.size());
        for (int i = 0, c = 0; i < scripts.size(); i++) {
            String path = scripts.get(i);
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

        ReadWriteLock rwlock = RW_STRIPES.get(fp);
        Lock lock = rwlock.writeLock();
        lock.lock();
        try {
            if (compileScript(fp, resources, kvspec) == null) { // script failed to compile
                return;
            }

            // OK then register script dependencies

            // todo

            activeFingerprints.add(fp);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            lock.unlock();
        }
    }

}
