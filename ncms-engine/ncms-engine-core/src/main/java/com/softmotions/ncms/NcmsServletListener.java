package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.softmotions.web.CharsetFilter;
import com.softmotions.web.security.SecurityFakeEnvFilter;
import com.softmotions.weboot.WBServletListener;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletListener extends WBServletListener {

    protected final org.slf4j.Logger log;

    private static final String LOGO =
            "                                                    \n" +
            " _____ _____ _____ _____    _____         _         \n" +
            "|   | |     |     |   __|  |   __|___ ___|_|___ ___ \n" +
            "| | | |   --| | | |__   |  |   __|   | . | |   | -_|\n" +
            "|_|___|_____|_|_|_|_____|  |_____|_|_|_  |_|_|_|___|\n" +
            "                                     |___|          \n" +
            " Environment: {}\n" +
            " Version: {}\n" +
            " Max heap: {}\n";

    private GuiceResteasyBootstrapServletContextListener resteasyBootstrap;

    public NcmsServletListener() {
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    protected String getLogo() {
        return LOGO;
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext sctx = event.getServletContext();
        sctx.setInitParameter("WEBOOT_CFG_CLASS", NcmsEnvironment.class.getName());

        Logger.setLoggerType(Logger.LoggerType.SLF4J);
        sctx.setInitParameter("resteasy.document.expand.entity.references", "false");
        sctx.setInitParameter("resteasy.role.based.security", "true");

        super.contextInitialized(event);

        NcmsEnvironment env = (NcmsEnvironment) sctx.getAttribute(WEBOOT_CFG_SCTX_KEY);
        resteasyBootstrap = getInjector().getInstance(GuiceResteasyBootstrapServletContextListener.class);
        resteasyBootstrap.contextInitialized(event);


        initCacheHeadersFilters(env, sctx);
        initJarResources(env, sctx);

        sctx.addFilter("charsetFilter", CharsetFilter.class)
            .addMappingForUrlPatterns(null, false, "/*");

        initSecurity(env, sctx);

        sctx.addFilter("guiceFilter", GuiceFilter.class)
            .addMappingForUrlPatterns(null, false, "/*");

        start();
    }


    private void initSecurity(NcmsEnvironment env, ServletContext sctx) {
        String webFakeUser = env.xcfg().getString("security.web-fakeuser");
        if (webFakeUser == null) {
            return;
        }
        String dbJndiName = env.xcfg().getString("security[@dbJndiName]");
        log.info("Setup SecurityFakeEnvFilter filter fake web user: {}", webFakeUser);
        if (StringUtils.isBlank(dbJndiName)) {
            throw new RuntimeException("Missing required 'dbJndiName' attribute in the <security> configuration");
        }
        FilterRegistration.Dynamic reg = sctx.addFilter(SecurityFakeEnvFilter.class.getName(),
                                                        SecurityFakeEnvFilter.class);
        reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
        reg.setInitParameter("dbJndiName", dbJndiName);
        reg.setInitParameter("username", webFakeUser);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        stop();
        if (resteasyBootstrap != null) {
            resteasyBootstrap.contextDestroyed(servletContextEvent);
            resteasyBootstrap = null;
        }
        super.contextDestroyed(servletContextEvent);
    }

    @Override
    protected Collection<Module> getStartupModules() {
        List<Module> mlist = new ArrayList<>(1);
        mlist.add(new NcmsServletModule());
        return mlist;
    }
}
