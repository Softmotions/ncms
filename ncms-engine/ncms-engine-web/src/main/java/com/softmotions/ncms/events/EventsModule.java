package com.softmotions.ncms.events;

import com.softmotions.ncms.NcmsConfiguration;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.concurrent.Executors;

/**
 * Application-wide event-bus based on Guava {@link com.google.common.eventbus.EventBus}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class EventsModule extends AbstractModule {

    protected void configure() {
        bind(EventBus.class).to(LocalEventBus.class).in(Singleton.class);
    }

    static class LocalEventBus extends AsyncEventBus {
        @Inject
        LocalEventBus(NcmsConfiguration cfg) {
            super(Executors.newFixedThreadPool(cfg.impl().getInt("events.num-workers", 3)));
        }
    }
}
