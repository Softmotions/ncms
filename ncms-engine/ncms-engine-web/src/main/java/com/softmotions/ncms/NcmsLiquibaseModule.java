package com.softmotions.ncms;

import com.google.inject.AbstractModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Liquibase Guice integration.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsLiquibaseModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsLiquibaseModule.class);

    final NcmsConfiguration cfg;

    public NcmsLiquibaseModule(NcmsConfiguration cfg) {
        this.cfg = cfg;
    }

    protected void configure() {

    }
}
