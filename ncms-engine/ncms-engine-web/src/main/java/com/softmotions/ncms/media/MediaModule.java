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
        bind(MediaRS.class).in(Singleton.class);
        bind(MediaService.class).to(MediaRS.class);
    }
}
