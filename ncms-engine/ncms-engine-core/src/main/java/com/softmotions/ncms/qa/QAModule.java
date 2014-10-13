package com.softmotions.ncms.qa;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class QAModule extends AbstractModule {

    protected void configure() {
        bind(PageQARS.class).in(Singleton.class);
    }
}
