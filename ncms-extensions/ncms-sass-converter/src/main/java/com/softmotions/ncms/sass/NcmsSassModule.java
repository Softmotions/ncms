package com.softmotions.ncms.sass;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;
import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.normalize;

import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.softmotions.commons.ThreadUtils;
import com.softmotions.commons.lifecycle.Dispose;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.atm.ServerMessageEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.ncms.media.events.MediaUpdateEvent;
import com.softmotions.weboot.executor.TaskExecutor;

/**
 * Sass converter module.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class NcmsSassModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsSassModule.class);

    @Override
    protected void configure() {
        bind(NcmsSassService.class).asEagerSingleton();
    }

    public static class NcmsSassService {

        private final NcmsEnvironment env;

        private final TaskExecutor executor;

        private final NcmsEventBus ebus;

        private final MediaRepository repository;

        @Inject
        public NcmsSassService(NcmsEnvironment env,
                               TaskExecutor executor,
                               MediaRepository repository,
                               NcmsEventBus ebus) {
            this.env = env;
            this.executor = executor;
            this.ebus = ebus;
            this.repository = repository;
        }

        @Start
        public void startup() {
            log.info("Starting nCMS SCSS converter module");
            ebus.register(this);
        }

        @Dispose
        public void shutdown() {
            ebus.unregister(this);
        }

        @Subscribe
        public void mediaUpdated(MediaUpdateEvent ev) {
            // ui=sideEditor,app=0ad30649-27e0-47a6-a45a-58ed8c1e125a}
            String path = ev.getPath();
            if (ev.isFolder()
                || (!path.endsWith(".scss") && !path.endsWith(".sass")) /* not a sass/scss file */
                || FilenameUtils.getName(path).startsWith("_") /* Ignore files with `_` prefix */) {
                return;
            }
            executor.submit(new NcmsSassConversionJob(this, ev));
        }
    }

    private static class NcmsSassConversionJob implements Runnable, Importer {

        private final NcmsSassService sassService;

        private final MediaUpdateEvent ev;

        private NcmsSassConversionJob(NcmsSassService sassService,
                                      MediaUpdateEvent ev) {
            this.sassService = sassService;
            this.ev = ev;
        }

        void reportError(String msg) {
            ServerMessageEvent err = new ServerMessageEvent(this, msg, true, true, null);
            String app = (String) ev.hints().get("app");
            if (app != null) {
                err.hint("app", app);
                sassService.ebus.fire(err);
            }
        }

        private static final Pattern CSTYLE_DIRECTIVE =
                Pattern.compile("@compile\\s+(compact|nested|expanded|compressed)\\s*;",
                                Pattern.CASE_INSENSITIVE);

        private static final Pattern COMMENTS_DIRECTIVE =
                Pattern.compile("@comments\\s+(true|false|yes|no)\\s*;",
                                Pattern.CASE_INSENSITIVE);

        private static final Pattern EMAP_DIRECTIVE =
                Pattern.compile("@emap\\s+(true|false|yes|no)\\s*;",
                                Pattern.CASE_INSENSITIVE);



        private void setOutputStyle(String style, Options opts) {
            //noinspection SwitchStatementWithoutDefaultBranch
            switch (style) {
                case "COMPACT":
                case "NESTED":
                case "EXPANDED":
                case "COMPRESSED":
                    opts.setOutputStyle(OutputStyle.valueOf(style));
            }
        }

        // run conversion job
        @Override
        public void run() {

            ThreadUtils.cleanInheritableThreadLocals();

            MediaRepository repo = sassService.repository;
            NcmsEnvironment env = sassService.env;
            String targetPath = ev.getPath().replaceAll("\\.scss$", ".css");
            MediaResource src = repo.findMediaResource(ev.getId(), null);
            if (src == null) { // media resource disappeared
                return;
            }

            Compiler scp = new Compiler();
            Options opts = new Options();
            opts.getImporters().add(this);

            setOutputStyle(env.xcfg().getString("media.sass.output-style", "COMPACT").toUpperCase(), opts);
            try {
                log.info("Sass compilation {} into {}", ev.getPath(), targetPath);
                String source = src.getSource();
                Matcher m = CSTYLE_DIRECTIVE.matcher(source);
                if (m.find()) {
                    setOutputStyle(m.group(1).toUpperCase(), opts);
                }
                m = COMMENTS_DIRECTIVE.matcher(source);
                if (m.find()) {
                    opts.setSourceComments(BooleanUtils.toBoolean(m.group(1)));
                }
                m = EMAP_DIRECTIVE.matcher(source);
                if (m.find()) {
                    opts.setSourceMapEmbed(BooleanUtils.toBoolean(m.group(1)));
                }
                Output output = scp.compileString(source, opts);
                String css = output.getCss() != null ? output.getCss() : "";
                try (InputStream is = IOUtils.toInputStream(css, "UTF-8")) {
                    repo.importFile(is, targetPath, false, src.getOwner());
                }
                log.info("Sass compilation {} into {} finished", ev.getPath(), targetPath);
            } catch (CompilationException e) {
                log.warn("Sass compilation error: ", e);
                reportError("Sass compilation error!\n" + e.getErrorMessage());
            } catch (Exception e) {
                log.warn("Sass compilation error!\nFile: {}", ev.getPath(), e);
                reportError(String.format("Sass compilation error! File: %s Error: %s", ev.getPath(), e.getMessage()));
            }
        }

        @Override
        public Collection<Import> apply(String url, Import previous) {
            //noinspection StringEqualsEmptyString
            if ("".equals(FilenameUtils.getExtension(url))) {
                url += ".scss";
            }
            String prev = previous.getAbsoluteUri().toString();
            if ("stdin".equals(prev)) {
                prev = ev.getPath();
            }
            String path = normalize(url.startsWith("/") ? url : concat('/' + FilenameUtils.getPath(prev), url));
            String targetFile = FilenameUtils.getName(path);
            if (!targetFile.startsWith("_")) {
                path = concat('/' + FilenameUtils.getPath(path), "_" + targetFile);
            }
            MediaRepository repo = sassService.repository;
            MediaResource importResource = repo.findMediaResource(path, null);
            if (importResource == null) {
                log.warn("Import resource: '{}' is not found", path);
                return null;
            }
            try {
                return Collections.singletonList(new Import(
                        new URI(url),
                        new URI(path),
                        importResource.getSource()
                ));
            } catch (Exception e) {
                log.warn("Failed to import sass file: {} in context: {}",
                         url, previous.getAbsoluteUri(), e);
            }
            return null;
        }
    }
}
