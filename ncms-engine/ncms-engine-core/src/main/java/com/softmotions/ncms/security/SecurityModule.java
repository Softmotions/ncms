package com.softmotions.ncms.security;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NcmsSecurityRS.class).in(Singleton.class);
    }
}
