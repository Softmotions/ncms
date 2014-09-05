package com.softmotions.ncms;

import com.softmotions.web.CharsetFilter;
import com.softmotions.weboot.WBServletListener;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletListener extends WBServletListener {

    private GuiceResteasyBootstrapServletContextListener resteasyBootstrap;

    public void contextInitialized(ServletContextEvent event) {
        ServletContext sctx = event.getServletContext();
        sctx.setInitParameter("WEBOOT_CFG_CLASS", NcmsConfiguration.class.getName());

        Logger.setLoggerType(Logger.LoggerType.SLF4J);
        sctx.setInitParameter("resteasy.document.expand.entity.references", "false");
        sctx.setInitParameter("resteasy.role.based.security", "true");

        super.contextInitialized(event);

        resteasyBootstrap = getInjector().getInstance(GuiceResteasyBootstrapServletContextListener.class);
        resteasyBootstrap.contextInitialized(event);

        sctx.addFilter("charsetFilter", CharsetFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");
        sctx.addFilter("guiceFilter", GuiceFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");

        start();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        stop();

        if (resteasyBootstrap != null) {
            resteasyBootstrap.contextDestroyed(servletContextEvent);
            resteasyBootstrap = null;
        }
        super.contextDestroyed(servletContextEvent);
    }

    protected Collection<Module> getStartupModules() {
        List<Module> mlist = new ArrayList<>(1);
        mlist.add(new NcmsServletModule());
        return mlist;
    }


}
