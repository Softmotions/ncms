package com.softmotions.ncms.adm;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Admin-zone GUI RESTFull modules.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AdmModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WorkspaceRS.class).in(Singleton.class);
        bind(AdmUIResourcesRS.class).in(Singleton.class);
    }
}
