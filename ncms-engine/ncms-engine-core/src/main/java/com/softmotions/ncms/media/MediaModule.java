package com.softmotions.ncms.media;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.utils.ZipUtils;
import com.softmotions.xconfig.XConfig;

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
            XConfig xcfg = env.xcfg();
            List<XConfig> hcl = xcfg.subPattern("media.import");
            for (final XConfig hc : hcl) {
                processImportDir(hc);
            }
        }

        private void processImportDir(XConfig c) {
            String target = c.text("target");
            if (StringUtils.isBlank(target)) {
                log.error("Missing required media.import.target configuration attribute");
                return;
            }
            int flags = 0;
            if (c.boolPattern("watch", false)) {
                flags |= MediaRepository.IMPORT_WATCH;
            }
            if (c.boolPattern("overwrite", false)) {
                flags |= MediaRepository.IMPORT_OVERWRITE;
            }
            if (c.boolPattern("system", false)) {
                flags |= MediaRepository.IMPORT_SYSTEM;
            }
            if (c.boolPattern("cleanupMissing", false)) {
                flags |= MediaRepository.IMPORT_CLEANUP_MISSING;
            }

            String[] includes = c.listPattern("includes.include").toArray(new String[0]);
            String[] excludes = c.listPattern("excludes.exclude").toArray(new String[0]);
            String unpack = env.xcfg().text("media.unpack-directory");
            if (StringUtils.isBlank(unpack)) {
                unpack = env.getSessionTmpdir().toPath().resolve("unpack").toString();
            }

            for (String srcDir : c.listPattern("directory")) {
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
