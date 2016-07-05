package com.softmotions.ncms.rds;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class RefDataStoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RefDataStore.class).in(Singleton.class);
    }
}
