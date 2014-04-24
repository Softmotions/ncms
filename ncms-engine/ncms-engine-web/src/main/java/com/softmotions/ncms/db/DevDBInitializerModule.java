package com.softmotions.ncms.db;

import ninja.lifecycle.Start;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initiates development database with initial data.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class DevDBInitializerModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(DevDBInitializerModule.class);

    protected void configure() {
        bind(DevDBInitializer.class).asEagerSingleton();
    }

    public static class DevDBInitializer {

        @Inject
        AsmDAO adao;

        @Start(order = 50)
        @Transactional
        public void init() throws Exception {
            log.info("Initializing development database with test data...");

            Asm asm = new Asm("pub.base");
            log.info("!=" + adao.asmInsertOrUpdate(asm));

            Asm asm2 = new Asm("pub.main");
            log.info("!=" + adao.asmInsertOrUpdate(asm2));
        }
    }
}
