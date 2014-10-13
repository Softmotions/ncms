package com.softmotions.ncms.qa;

import com.softmotions.ncms.NcmsEnvironment;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class QAModule extends AbstractModule {

    private final NcmsEnvironment env;

    public QAModule(NcmsEnvironment env) {
        this.env = env;
    }

    protected void configure() {
        bind(PageQARS.class).in(Singleton.class);
    }
}
