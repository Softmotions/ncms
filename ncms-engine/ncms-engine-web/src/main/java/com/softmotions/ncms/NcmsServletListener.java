package com.softmotions.ncms;

import ninja.servlet.NinjaServletListener;
import ninja.utils.NinjaModeHelper;
import ninja.utils.NinjaProperties;
import ninja.utils.NinjaPropertiesImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * NCMS engine startup listener.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletListener implements ServletContextListener {

    public static final String NCMS_NINJA_PROPS_SCTX_KEY = "com.softmotions.ncms.NINJA_PROPS";

    final NinjaServletListener ninjaServletListener;

    NinjaProperties ninjaProperties;

    public NcmsServletListener() {
        ninjaServletListener = new NinjaServletListener();
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (ninjaProperties == null) {
            ninjaProperties = new NinjaPropertiesImpl(
                    NinjaModeHelper.determineModeFromSystemPropertiesOrProdIfNotSet());
            ninjaServletListener.setNinjaProperties((NinjaPropertiesImpl) ninjaProperties);
        }
        servletContextEvent.getServletContext().setAttribute(NCMS_NINJA_PROPS_SCTX_KEY, ninjaProperties);
        ninjaServletListener.contextInitialized(servletContextEvent);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            ninjaServletListener.contextDestroyed(servletContextEvent);
        } finally {
            servletContextEvent.getServletContext().removeAttribute(NCMS_NINJA_PROPS_SCTX_KEY);
        }
    }
}
