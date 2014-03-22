package com.softmotions.ncms.engine;

import com.google.inject.AbstractModule;

/**
 * Ncms Guice services.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsGuiceModule extends AbstractModule {

    private final NcmsConfiguration cfg;

    public NcmsGuiceModule(NcmsConfiguration cfg) {
        this.cfg = cfg;
    }

    protected void configure() {
        bind(NcmsConfiguration.class).toInstance(cfg);
    }
}
