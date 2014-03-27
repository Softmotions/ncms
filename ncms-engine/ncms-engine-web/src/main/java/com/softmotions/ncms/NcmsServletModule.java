package com.softmotions.ncms;

import ninja.servlet.NinjaServletDispatcher;
import ninja.utils.NinjaProperties;

import com.google.inject.servlet.ServletModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NCMS engine servlet module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletModule extends ServletModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsServletModule.class);

    protected void configureServlets() {
        log.info("Configure NCMS servlets");
        NinjaProperties nprops =
                (NinjaProperties) getServletContext()
                        .getAttribute(NcmsServletListener.NCMS_NINJA_PROPS_SCTX_KEY);
        if (nprops == null) {
            throw new RuntimeException("Unable to find Ninja framework properties in " +
                                       "ServletContext#" + NcmsServletListener.NCMS_NINJA_PROPS_SCTX_KEY
                                       + " attribute");
        }

        initNinja();
    }

    protected void initNinja() {
        //Ninja init part
        bind(NinjaServletDispatcher.class).asEagerSingleton();
        serve("/*").with(NinjaServletDispatcher.class);
    }
}
