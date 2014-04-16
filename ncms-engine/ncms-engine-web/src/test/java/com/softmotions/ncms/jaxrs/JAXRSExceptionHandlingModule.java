package com.softmotions.ncms.jaxrs;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class JAXRSExceptionHandlingModule extends AbstractModule {

    protected void configure() {
        bind(JAXRSExceptionHandlingRS.class).in(Singleton.class);
    }
}
