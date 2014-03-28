package com.softmotions.ncms.db;

import com.google.inject.AbstractModule;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced NCMS JPA Module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsDBModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsDBModule.class);

    private final SubnodeConfiguration cfg;

    public NcmsDBModule(SubnodeConfiguration cfg) {
        this.cfg = cfg;
    }

    protected void configure() {
        log.info("Initating NcmsDBModule");
    }
}
