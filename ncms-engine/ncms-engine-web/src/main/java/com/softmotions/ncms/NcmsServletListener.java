package com.softmotions.ncms;

import com.softmotions.commons.weboot.WBServletListener;

import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import javax.servlet.ServletContextEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletListener extends WBServletListener {

    private GuiceResteasyBootstrapServletContextListener resteasyBootstrap;

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Logger.setLoggerType(Logger.LoggerType.SLF4J);
        super.contextInitialized(servletContextEvent);
        resteasyBootstrap = getInjector().getInstance(GuiceResteasyBootstrapServletContextListener.class);
        resteasyBootstrap.contextInitialized(servletContextEvent);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (resteasyBootstrap != null) {
            resteasyBootstrap.contextDestroyed(servletContextEvent);
            resteasyBootstrap = null;
        }
        super.contextDestroyed(servletContextEvent);
    }
}
