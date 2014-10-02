package com.softmotions.ncms.ds;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class GeneralDataStoreModule extends AbstractModule {

    protected void configure() {
        bind(GeneralDataStore.class).in(Singleton.class);
    }
}
