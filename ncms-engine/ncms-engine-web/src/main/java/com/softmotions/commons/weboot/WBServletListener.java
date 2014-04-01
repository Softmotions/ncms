package com.softmotions.commons.weboot;

import ninja.servlet.NinjaServletListener;
import ninja.utils.NinjaModeHelper;
import ninja.utils.NinjaProperties;
import ninja.utils.NinjaPropertiesImpl;

import com.google.inject.Injector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Weboot engine startup listener.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class WBServletListener implements ServletContextListener {

    public static final String WB_NINJA_PROPS_SCTX_KEY = "com.softmotions.weboot.NINJA_PROPS";

    final NinjaServletListener ninjaServletListener;

    NinjaProperties ninjaProperties;

    public WBServletListener() {
        ninjaServletListener = new NinjaServletListener();
    }

    public WBServletListener(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
        ninjaServletListener = new NinjaServletListener();
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (ninjaProperties == null) {
            ninjaProperties = new NinjaPropertiesImpl(
                    NinjaModeHelper.determineModeFromSystemPropertiesOrProdIfNotSet());
            ninjaServletListener.setNinjaProperties((NinjaPropertiesImpl) ninjaProperties);
        }
        servletContextEvent.getServletContext().setAttribute(WB_NINJA_PROPS_SCTX_KEY, ninjaProperties);
        ninjaServletListener.contextInitialized(servletContextEvent);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            ninjaServletListener.contextDestroyed(servletContextEvent);
        } finally {
            servletContextEvent.getServletContext().removeAttribute(WB_NINJA_PROPS_SCTX_KEY);
        }
    }

    public Injector getInjector() {
        return ninjaServletListener.getInjector();
    }
}
