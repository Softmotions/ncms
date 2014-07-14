package com.softmotions.ncms.events;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.weboot.mb.MBSqlSessionListener;
import com.softmotions.weboot.mb.MBSqlSessionManager;

import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

/**
 * Application-wide event-bus based on Guava {@link com.google.common.eventbus.EventBus}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class EventsModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(EventsModule.class);

    protected void configure() {
        bind(NcmsEventBus.class).to(LocalEventBus.class).in(Singleton.class);
    }

    static class LocalEventBus extends AsyncEventBus implements NcmsEventBus {

        final MBSqlSessionManager sessionManager;

        public void fire(Object event) {
            post(event);
        }

        @Inject
        LocalEventBus(NcmsConfiguration cfg,
                      MBSqlSessionManager sessionManager) {
            super(Executors.newFixedThreadPool(cfg.impl().getInt("events.num-workers", 1)));
            this.sessionManager = sessionManager;
        }

        public void fireOnSuccessCommit(final Object event) {
            sessionManager.registerNextEventSessionListener(new MBSqlSessionListener() {
                public void commit(boolean success) {
                    if (success) {
                        fire(event);
                    }
                }

                public void close(boolean success) {
                    if (success) {
                        fire(event);
                    }
                }

                public void rollback() {
                }

            });
        }

        public void fireOnRollback(final Object event) {
            sessionManager.registerNextEventSessionListener(new MBSqlSessionListener() {
                public void commit(boolean success) {
                    if (!success) {
                        fire(event);
                    }
                }

                public void close(boolean success) {
                    if (!success) {
                        fire(event);
                    }
                }

                public void rollback() {
                    fire(event);
                }
            });
        }
    }
}
