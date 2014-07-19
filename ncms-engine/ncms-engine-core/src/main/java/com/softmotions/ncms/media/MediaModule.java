package com.softmotions.ncms.media;

import ninja.lifecycle.Start;
import com.softmotions.ncms.NcmsConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Media guice components.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(MediaModule.class);

    protected void configure() {
        bind(MediaRS.class).in(Singleton.class);
        bind(MediaRepository.class).to(MediaRS.class);
        bind(MediaReader.class).to(MediaRS.class);
        bind(MediaModuleInitializer.class).asEagerSingleton();
    }


    public static class MediaModuleInitializer {

        final NcmsConfiguration cfg;

        final MediaRepository mediaRepository;

        @Inject
        public MediaModuleInitializer(NcmsConfiguration cfg, MediaRepository mediaRepository) {
            this.cfg = cfg;
            this.mediaRepository = mediaRepository;
        }

        @Start
        public void start() throws Exception {
            XMLConfiguration xcfg = cfg.impl();
            List<HierarchicalConfiguration> hcl = xcfg.configurationsAt("media.import");
            for (final HierarchicalConfiguration hc : hcl) {
                processImportDir(hc);
            }
        }

        private void processImportDir(HierarchicalConfiguration c) {
            String srcDir = c.getString("[@directory]");
            if (StringUtils.isBlank(srcDir)) {
                log.error("Missing required media.import[@directory] configuration attribute");
                return;
            }
            String target = c.getString("[@target]");
            if (StringUtils.isBlank(target)) {
                log.error("Missing required media.import[@target] configuration attribute");
                return;
            }
            Path srcPath = Paths.get(srcDir).toAbsolutePath().normalize();
            if (!Files.isDirectory(srcPath)) {
                log.error("Failed to import: " + srcPath + " is not a directory");
                return;
            }
            boolean watch = c.getBoolean("[@watch]", false);
            boolean overwrite = c.getBoolean("[@overwrite]", false);
            boolean system = c.getBoolean("[@system]", false);
            List<String> includes = new ArrayList<>();
            List<String> excludes = new ArrayList<>();
            for (Object o : c.getList("includes.include")) {
                includes.add(String.valueOf(o));
            }
            for (Object o : c.getList("excludes.exclude")) {
                excludes.add(String.valueOf(o));
            }
            try {
                mediaRepository.importDirectory(srcPath.toString(),
                                                target,
                                                includes.toArray(new String[includes.size()]),
                                                excludes.toArray(new String[excludes.size()]),
                                                overwrite,
                                                watch,
                                                system);
            } catch (IOException e) {
                log.error("Failed to import directory: ", e);
            }

        }


    }
}
