package com.softmotions.ncms.adm;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Admin-zone GUI RESTFull modules.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AdmRestModule extends AbstractModule {

    protected void configure() {
        bind(WorkspaceRS.class).in(Singleton.class);
        bind(UIResourcesRS.class).in(Singleton.class);
    }
}
