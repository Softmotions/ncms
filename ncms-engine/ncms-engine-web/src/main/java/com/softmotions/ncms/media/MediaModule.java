package com.softmotions.ncms.media;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Media guice components.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaModule extends AbstractModule {

    protected void configure() {
        bind(MediaDAO.class).in(Singleton.class);
        bind(MediaRS.class).in(Singleton.class);
    }
}
