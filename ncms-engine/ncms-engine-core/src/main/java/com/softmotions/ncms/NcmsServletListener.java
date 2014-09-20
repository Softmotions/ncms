package com.softmotions.ncms;

import com.softmotions.web.CharsetFilter;
import com.softmotions.web.security.SecurityFakeEnvFilter;
import com.softmotions.weboot.WBServletListener;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletListener extends WBServletListener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(NcmsServletListener.class);

    private static final String LOGO =
            "                                                    \n" +
            " _____ _____ _____ _____    _____         _         \n" +
            "|   | |     |     |   __|  |   __|___ ___|_|___ ___ \n" +
            "| | | |   --| | | |__   |  |   __|   | . | |   | -_|\n" +
            "|_|___|_____|_|_|_|_____|  |_____|_|_|_  |_|_|_|___|\n" +
            "                                     |___|          \n" +
            "Version: {}                                         \n";

    private GuiceResteasyBootstrapServletContextListener resteasyBootstrap;

    public void contextInitialized(ServletContextEvent event) {
        ServletContext sctx = event.getServletContext();
        sctx.setInitParameter("WEBOOT_CFG_CLASS", NcmsConfiguration.class.getName());

        Logger.setLoggerType(Logger.LoggerType.SLF4J);
        sctx.setInitParameter("resteasy.document.expand.entity.references", "false");
        sctx.setInitParameter("resteasy.role.based.security", "true");

        super.contextInitialized(event);

        NcmsConfiguration cfg = (NcmsConfiguration) sctx.getAttribute(WEBOOT_CFG_SCTX_KEY);
        resteasyBootstrap = getInjector().getInstance(GuiceResteasyBootstrapServletContextListener.class);
        resteasyBootstrap.contextInitialized(event);

        sctx.addFilter("charsetFilter", CharsetFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");

        initSecurity(cfg, sctx);

        sctx.addFilter("guiceFilter", GuiceFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");

        start();

        log.info(LOGO, cfg.getNcmsVersion());
    }


    private void initSecurity(NcmsConfiguration cfg, ServletContext sctx) {
        String webFakeUser = cfg.impl().getString("security.web-fakeuser");
        if (webFakeUser == null) {
            return;
        }
        String dbJndiName = cfg.impl().getString("security[@dbJndiName]");
        log.info("Setup SecurityFakeEnvFilter filter fake web user: " + webFakeUser);
        if (StringUtils.isBlank(dbJndiName)) {
            throw new RuntimeException("Missing required 'dbJndiName' attribute in the <security> configuration");
        }
        FilterRegistration.Dynamic reg = sctx.addFilter(SecurityFakeEnvFilter.class.getName(),
                                                        SecurityFakeEnvFilter.class);
        reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
        reg.setInitParameter("dbJndiName", dbJndiName);
        reg.setInitParameter("username", webFakeUser);

    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            stop();
            if (resteasyBootstrap != null) {
                resteasyBootstrap.contextDestroyed(servletContextEvent);
                resteasyBootstrap = null;
            }
            super.contextDestroyed(servletContextEvent);
        } finally {
            NcmsConfiguration.INSTANCE = null;
        }
    }

    protected Collection<Module> getStartupModules() {
        List<Module> mlist = new ArrayList<>(1);
        mlist.add(new NcmsServletModule());
        return mlist;
    }
}
