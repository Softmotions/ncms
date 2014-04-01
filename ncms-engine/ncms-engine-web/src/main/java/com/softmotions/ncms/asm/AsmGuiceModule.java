package com.softmotions.ncms.asm;

import com.google.inject.AbstractModule;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmGuiceModule extends AbstractModule {

    protected void configure() {
        bind(AsmDAO.class);
    }
}
