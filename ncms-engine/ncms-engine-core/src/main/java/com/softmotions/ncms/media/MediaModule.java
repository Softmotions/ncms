package com.softmotions.ncms.media;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.commons.zip.ZipUtils;
import com.softmotions.ncms.NcmsEnvironment;

/**
 * Media guice components.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
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

            String target = c.getString("target");
            if (StringUtils.isBlank(target)) {
                log.error("Missing required media.import.target configuration attribute");
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

            String[] includes = c.getList("includes.include").stream()
                                 .map(String::valueOf)
                                 .toArray(String[]::new);
            String[] excludes = c.getList("excludes.exclude").stream()
                                 .map(String::valueOf)
                                 .toArray(String[]::new);

            String unpack = env.xcfg().getString("media.unpack-directory", null);
            if (StringUtils.isBlank(unpack)) {
                unpack = env.getSessionTmpdir().toPath().resolve("unpack").toString();
            }

            for (Object v : c.getList("directory")) {
                String srcDir = (v != null ? v.toString() : null);
                if (StringUtils.isBlank(srcDir)) {
                    log.error("Missing required media.import.directory configuration attribute");
                    continue;
                }
                Path srcPath = Paths.get(srcDir).toAbsolutePath().normalize();
                Path srcFileNamePath = srcPath.getFileName();
                String srcFileName = (srcFileNamePath != null) ? srcFileNamePath.toString() : null;
                if (srcFileName != null && StringUtils.endsWithAny(srcFileName.toLowerCase(), ".zip", ".jar")) {
                    if (!Files.isRegularFile(srcPath)) {
                        log.error("File: {} is not a regular JAR archive", srcDir);
                        continue;
                    }
                    // we have specified jar file as directory, lets unpack it
                    // to the directory specified by 'media/unpack-directory' parameter
                    // or {newtmp}/unpack if this parameter us unspecified
                    File unpackFile = new File(unpack);
                    if (!unpackFile.isDirectory()) {
                        unpackFile.mkdirs();
                    }
                    if (!unpackFile.isDirectory()) {
                        log.error("Failed to create unpack directory: {}", unpackFile);
                    } else {
                        log.info("Using unpack directory: {}", unpackFile);
                    }
                    File unpackTarget = unpackFile.toPath()
                                                  .resolve(srcFileName.substring(0, srcFileName.length() - 4))
                                                  .toFile();
                    if (unpackTarget.exists()) {
                        FileUtils.deleteQuietly(unpackTarget);
                    }
                    try {
                        ZipUtils.unjarFile(srcPath.toFile(), unpackFile);
                    } catch (IOException e) {
                        log.error("Failed to unpack jar file: {} to {}", srcPath, unpackFile, e);
                        continue;
                    }
                    srcPath = unpackTarget.toPath();
                }
                if (!Files.isDirectory(srcPath)) {
                    log.error("Failed to import: {} is not a directory", srcPath);
                    continue;
                }
                try {
                    mediaRepository.importDirectory(srcPath.toString(),
                                                    target,
                                                    includes,
                                                    excludes,
                                                    flags);
                } catch (IOException e) {
                    log.error("Failed to import directory: ", e);
                }
                break;
            }
        }
    }
}
