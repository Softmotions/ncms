package com.softmotions.ncms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.softmotions.commons.lifecycle.Start;

/**
 * Display NCM logo after startup.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsLogoModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsLogoModule.class);

    private static final String LOGO =
            "                                                    \n" +
            " _____ _____ _____ _____    _____         _         \n" +
            "|   | |     |     |   __|  |   __|___ ___|_|___ ___ \n" +
            "| | | |   --| | | |__   |  |   __|   | . | |   | -_|\n" +
            "|_|___|_____|_|_|_|_____|  |_____|_|_|_  |_|_|_|___|\n" +
            "                                     |___|          \n" +
            " Environment: %s\n" +
            " Version: %s\n" +
            " Max heap: %s\n";


    @Override
    protected void configure() {
        bind(LogoStarter.class).asEagerSingleton();
    }


    public static class LogoStarter {

        final NcmsEnvironment env;

        @Inject
        public LogoStarter(NcmsEnvironment env) {
            this.env = env;
        }

        @Start(order = Integer.MAX_VALUE)
        public void startup() {
            log.info(String.format(LOGO, env.getEnvironmentType(), env.getAppVersion(), Runtime.getRuntime().maxMemory()));
        }
    }
}
