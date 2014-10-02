package ru.nsu.legacy;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NSULegacyModule extends AbstractModule {

    protected void configure() {
        bind(NSULegacyRS.class).in(Singleton.class);

    }
}
