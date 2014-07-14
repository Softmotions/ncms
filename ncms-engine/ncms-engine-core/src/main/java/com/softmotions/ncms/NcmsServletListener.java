package com.softmotions.ncms;

import ninja.utils.NinjaProperties;
import com.softmotions.web.CharsetFilter;
import com.softmotions.weboot.WBServletListener;

import com.google.inject.servlet.GuiceFilter;

import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletListener extends WBServletListener {

    private GuiceResteasyBootstrapServletContextListener resteasyBootstrap;

    public NcmsServletListener(NinjaProperties ninjaProperties) {
        super(ninjaProperties);
    }

    public void contextInitialized(ServletContextEvent event) {
        Logger.setLoggerType(Logger.LoggerType.SLF4J);
        ServletContext sctx = event.getServletContext();
        sctx.setInitParameter("resteasy.document.expand.entity.references", "false");
        sctx.setInitParameter("resteasy.role.based.security", "true");

        super.contextInitialized(event);

        resteasyBootstrap = getInjector().getInstance(GuiceResteasyBootstrapServletContextListener.class);
        resteasyBootstrap.contextInitialized(event);

        sctx.addFilter("charsetFilter", CharsetFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");

        sctx.addFilter("guiceFilter", GuiceFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");

    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (resteasyBootstrap != null) {
            resteasyBootstrap.contextDestroyed(servletContextEvent);
            resteasyBootstrap = null;
        }
        super.contextDestroyed(servletContextEvent);
    }
}
