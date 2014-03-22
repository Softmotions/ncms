package com.softmotions.ncms.engine;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice servlet context listener used to start standalone Ncms engine.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletContextListener extends GuiceServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(NcmsServletContextListener.class);

    protected Injector getInjector() {
        log.info("BUILDING NCMS GUICE INJECTOR");
        NcmsConfiguration cfg = new NcmsConfiguration();
        return Guice.createInjector(new NcmsGuiceModule(cfg), new NcmsServletModule(cfg));
    }
}
