package com.softmotions.ncms.engine;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
        initLogging(cfg);
        return Guice.createInjector(new NcmsGuiceModule(cfg), new NcmsServletModule(cfg));
    }


    void initLogging(NcmsConfiguration cfg) {
        XMLConfiguration xcfg = cfg.impl();
        String logCfg = xcfg.getString("logging-configuration");
        if (logCfg == null) {
            log.warn("Logging <logging-configuration> not found");
            return;
        }
        ClassLoader cl = ObjectUtils.firstNonNull(Thread.currentThread().getContextClassLoader(),
                                                  getClass().getClassLoader());
        URL logCfgUrl = cl.getResource(logCfg);
        if (logCfgUrl == null) {
            File f = new File(logCfg);
            if (f.exists()) {
                try {
                    logCfgUrl = f.toURI().toURL();
                } catch (MalformedURLException e) {
                    ;
                }
            }
        }
        if (logCfgUrl == null) {
            try {
                logCfgUrl = new URL(logCfg);
            } catch (MalformedURLException e) {
                ;
            }
        }
        if (logCfgUrl == null) {
            log.error("Unable to find the <logging-configuration> resource: " + logCfg);
            return;
        }
        log.info("Configuring logging according to: " + logCfgUrl);
        LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(logContext);
            logContext.reset();
            configurator.doConfigure(logCfgUrl);
        } catch (JoranException je) {
            ;
        }
        log.info("Successfully configured application logging from: ", logCfg);
        StatusPrinter.printInCaseOfErrorsOrWarnings(logContext);
    }
}
