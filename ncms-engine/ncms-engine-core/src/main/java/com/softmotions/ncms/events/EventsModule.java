package com.softmotions.ncms.events;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.weboot.mb.MBSqlSessionListener;
import com.softmotions.weboot.mb.MBSqlSessionManager;

/**
 * Application-wide event-bus based on Guava {@link com.google.common.eventbus.EventBus}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class EventsModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(EventsModule.class);

    private static final NcmsSubscriberExceptionHandler EX_INSTANCE = new NcmsSubscriberExceptionHandler();

    protected void configure() {
        bind(NcmsEventBus.class).to(LocalEventBus.class).in(Singleton.class);
    }

    static class NcmsSubscriberExceptionHandler implements SubscriberExceptionHandler {
        public void handleException(Throwable ex, SubscriberExceptionContext ctx) {
            log.error("Could not dispatch event: " + ctx.getSubscriber() +
                      " to " + ctx.getSubscriberMethod(), ex);
        }
    }

    static class LocalEventBus extends AsyncEventBus implements NcmsEventBus {

        final MBSqlSessionManager sessionManager;

        public void fire(Object event) {
            post(event);
        }

        @Inject
        LocalEventBus(NcmsEnvironment env,
                      MBSqlSessionManager sessionManager) {
            super(Executors.newFixedThreadPool(env.xcfg().getInt("events.num-workers", 1)), EX_INSTANCE);
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
