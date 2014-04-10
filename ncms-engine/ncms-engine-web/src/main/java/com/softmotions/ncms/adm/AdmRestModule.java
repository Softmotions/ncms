package com.softmotions.ncms.adm;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AdmRestModule extends AbstractModule {

    protected void configure() {
        bind(WorkspaceRS.class).in(Singleton.class);
    }
}
