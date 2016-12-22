package com.softmotions.ncms.hrs;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * REST helpers module.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class HrsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NewsHelperRS.class).in(Singleton.class);
    }
}
