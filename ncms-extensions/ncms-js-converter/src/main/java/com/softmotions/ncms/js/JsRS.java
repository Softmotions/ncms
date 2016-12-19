package com.softmotions.ncms.js;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
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
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * JS compiler service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@Path("adm/rs/x/js")
public class JsRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(JsRS.class);

    private static final Pattern HASH_REGEXP = Pattern.compile("^[0-9a-f]{32}(\\.js)?$");

    private static final Striped<ReadWriteLock> RW_STRIPES = Striped.lazyWeakReadWriteLock(256);

    private final File jsCache;

    private final MediaReader mediaReader;


    @Inject
    public JsRS(SqlSession sess,
                MediaReader mediaReader) throws IOException {
        super(JsRS.class.getName(), sess);
        this.mediaReader = mediaReader;
        this.jsCache = new File(mediaReader.getBaseDir(), ".jscache");
        DirUtils.ensureDir(jsCache, true);
    }

    /**
     * Retrieve compiled script by its fingerprint
     */
    @Path("/script/{fingerprint}")
    @Produces("application/javascript;charset=UTF-8")
    public String script(@PathParam("fingerprint") String fingerprint) throws Exception {
        Matcher m = HASH_REGEXP.matcher(fingerprint);
        if (!m.matches()) {
            throw new BadRequestException();
        }
        if (m.group(1) == null) {
            fingerprint += ".js";
        }
        File jsFile = new File(jsCache, fingerprint);
        String data = null;
        ReadWriteLock rwlock = RW_STRIPES.get(fingerprint);
        Lock lock = rwlock.readLock();
        try {
            if (jsFile.exists()) {
                data = FileUtils.readFileToString(jsFile, "utf-8");
            }
        } finally {
            lock.unlock();
        }
        if (data == null) {
            lock = rwlock.writeLock();
            try {
                if (jsFile.exists()) {
                    data = FileUtils.readFileToString(jsFile, "utf-8");
                } else {
                    data = recompileScript(fingerprint);
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

    @Nullable
    String recompileScript(String fingerprint) throws Exception {
        String spec = selectOne("selectScriptSpec", fingerprint);
        return spec != null ? compileScript(fingerprint, new KVOptions(spec)) : null;
    }

    @Nullable
    String compileScript(String hash, KVOptions spec) throws Exception {
        spec = (KVOptions) spec.clone();
        Long[] ids = Arrays.stream(spec.getOrDefault("scripts", "")
                                       .split(",")).map(Long::parseLong)
                           .toArray(Long[]::new);

        spec.remove("scripts");

        List<SourceFile> externs = new ArrayList<>();
        externs.addAll(CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER));
        List<SourceFile> inputs = new ArrayList<>(ids.length);

        for (int i = 0; i < ids.length; ++i) {
            MediaResource meta = mediaReader.findMediaResource(ids[i], null);
            if (meta == null) {
                String path = selectOne("selectScriptPathById", ids[i]);
                if (path == null) {
                    path = ids[i].toString();
                }
                log.error("Missing JS resource: {} hash: {} " +
                          "Script file will be compiled without this resource",
                          path, hash);
            } else {
                inputs.add(SourceFile.fromCode(meta.getName(), meta.getSource()));
            }
        }

        CompilerOptions options = new CompilerOptions();
        Compiler compiler = new Compiler(new ClosureLoggerErrorManager(log));

        options.setOutputCharset(Charset.forName("utf-8"));

        String val = spec.getOrDefault("languageIn", LanguageMode.ECMASCRIPT5.toString());
        options.setLanguageIn(LanguageMode.valueOf(val));

        val = spec.getOrDefault("languageOut", LanguageMode.ECMASCRIPT5.toString());
        options.setLanguageOut(LanguageMode.valueOf(val));

        Result result = compiler.compile(externs, inputs, options);
        if (!result.success) {
            return null;
        }

        String data = compiler.toSource();
        FileUtils.write(new File(jsCache, hash + ".js"), data, "utf-8", false);
        return data;
    }


}
