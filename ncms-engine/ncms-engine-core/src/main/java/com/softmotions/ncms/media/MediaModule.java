package com.softmotions.ncms.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.NcmsEnvironment;

/**
 * Media guice components.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(MediaModule.class);

    @Override
    protected void configure() {
        bind(MediaRS.class).in(Singleton.class);
        bind(MediaRepository.class).to(MediaRS.class);
        bind(MediaReader.class).to(MediaRS.class);
        bind(MediaModuleInitializer.class).asEagerSingleton();
    }

    public static class MediaModuleInitializer {

        final NcmsEnvironment env;

        final MediaRepository mediaRepository;

        @Inject
        public MediaModuleInitializer(NcmsEnvironment env, MediaRepository mediaRepository) {
            this.env = env;
            this.mediaRepository = mediaRepository;
        }

        @Start
        public void start() throws Exception {
            HierarchicalConfiguration<ImmutableNode> xcfg = env.xcfg();
            Iterator<String> mkeys = xcfg.getKeys("media");
            while (mkeys.hasNext()) {
                String mk = mkeys.next();
                log.info("{}: {}", mk, xcfg.getString(mk));
            }
            List<HierarchicalConfiguration<ImmutableNode>> hcl = xcfg.configurationsAt("media.import");
            for (final HierarchicalConfiguration hc : hcl) {
                processImportDir(hc);
            }
        }

        private void processImportDir(HierarchicalConfiguration c) {
            String srcDir = c.getString("directory");
            if (StringUtils.isBlank(srcDir)) {
                log.error("Missing required media.import.directory configuration attribute");
                return;
            }
            String target = c.getString("target");
            if (StringUtils.isBlank(target)) {
                log.error("Missing required media.import.target configuration attribute");
                return;
            }
            Path srcPath = Paths.get(srcDir).toAbsolutePath().normalize();
            if (!Files.isDirectory(srcPath)) {
                log.error("Failed to import: {} is not a directory", srcPath);
                return;
            }

            int flags = 0;
            if (c.getBoolean("watch", false)) {
                flags |= MediaRepository.IMPORT_WATCH;
            }
            if (c.getBoolean("overwrite", false)) {
                flags |= MediaRepository.IMPORT_OVERWRITE;
            }
            if (c.getBoolean("system", false)) {
                flags |= MediaRepository.IMPORT_SYSTEM;
            }
            if (c.getBoolean("cleanupMissing", false)) {
                flags |= MediaRepository.IMPORT_CLEANUP_MISSING;
            }

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
                                                flags);
            } catch (IOException e) {
                log.error("Failed to import directory: ", e);
            }
        }
    }
}
