package com.softmotions.ncms.sass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.softmotions.commons.lifecycle.Dispose;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.events.MediaUpdateEvent;
import com.softmotions.weboot.executor.TaskExecutor;

/**
 * Sass converter module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
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
            log.info("Starting nCMS SASS converter module");
            this.ebus.register(this);
        }

        @Dispose
        public void shutdown() {
            this.ebus.unregister(this);
        }

        @Subscribe
        public void mediaUpdated(MediaUpdateEvent ev) {
            log.info("Media updated: {}", ev.getPath());
        }
    }

    private static class NcmsSassConversionJob implements Runnable {

        private final NcmsSassService sassService;

        private NcmsSassConversionJob(NcmsSassService sassService) {
            this.sassService = sassService;
        }

        // run conversion job
        @Override
        public void run() {
            // todo
        }
    }
}
