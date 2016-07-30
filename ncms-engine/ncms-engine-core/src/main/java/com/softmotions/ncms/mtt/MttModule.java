package com.softmotions.ncms.mtt;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.softmotions.ncms.mtt.http.MttHttpModule;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MttModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MttRulesRS.class).in(Singleton.class);
        install(new MttHttpModule());
    }
}
