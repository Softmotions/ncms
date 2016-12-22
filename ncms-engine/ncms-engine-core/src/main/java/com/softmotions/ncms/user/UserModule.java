package com.softmotions.ncms.user;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * User environment/settings module.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class UserModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserEnvRS.class).in(Singleton.class);
    }
}
