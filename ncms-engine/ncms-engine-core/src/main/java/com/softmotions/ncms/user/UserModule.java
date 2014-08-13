package com.softmotions.ncms.user;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * User environment/settings module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class UserModule extends AbstractModule {

    protected void configure() {
        bind(UserEnvRS.class).in(Singleton.class);
    }
}
