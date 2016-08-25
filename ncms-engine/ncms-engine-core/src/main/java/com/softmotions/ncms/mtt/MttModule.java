package com.softmotions.ncms.mtt;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.softmotions.ncms.mtt.http.MttHttpModule;
import com.softmotions.ncms.mtt.tp.MttTpRS;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MttModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MttRulesRS.class).in(Singleton.class);
        bind(MttTpRS.class).in(Singleton.class);
        install(new MttHttpModule());
    }
}
