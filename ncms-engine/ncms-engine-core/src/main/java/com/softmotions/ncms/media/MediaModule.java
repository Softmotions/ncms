package com.softmotions.ncms.media;

import ninja.lifecycle.Start;
import com.softmotions.ncms.NcmsConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
        bind(MediaService.class).to(MediaRS.class);
        bind(MediaModuleInitializer.class).asEagerSingleton();
    }


    public static class MediaModuleInitializer {

        final NcmsConfiguration cfg;

        final MediaService mediaService;

        @Inject
        public MediaModuleInitializer(NcmsConfiguration cfg, MediaService mediaService) {
            this.cfg = cfg;
            this.mediaService = mediaService;
        }

        @Start
        public void start() throws Exception {
            XMLConfiguration xcfg = cfg.impl();
            List<HierarchicalConfiguration> hcl = xcfg.configurationsAt("media.import-directory");
            for (final HierarchicalConfiguration hc : hcl) {
                String directory = hc.getString(".");
                mediaService.importDirectory(new File(directory));
            }
        }
    }
}
