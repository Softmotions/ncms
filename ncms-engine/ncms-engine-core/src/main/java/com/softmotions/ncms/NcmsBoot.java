package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.SessionSupport;
import org.atmosphere.interceptor.ShiroInterceptor;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.softmotions.ncms.atm.NcmsAtmosphereObjectFactory;
import com.softmotions.ncms.security.NcmsGuardFilter;
import com.softmotions.ncms.shiro.NcmsShiroWebEnvironment;
import com.softmotions.ncms.utils.GzipFilter;
import com.softmotions.web.CharsetFilter;
import com.softmotions.weboot.WBConfiguration;
import com.softmotions.weboot.WBServletListener;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class NcmsBoot extends WBServletListener {

    private static final String LOGO =
            "                                                    \n" +
            " _____ _____ _____ _____    _____         _         \n" +
            "|   | |     |     |   __|  |   __|___ ___|_|___ ___ \n" +
            "| | | |   --| | | |__   |  |   __|   | . | |   | -_|\n" +
            "|_|___|_____|_|_|_|_____|  |_____|_|_|_  |_|_|_|___|\n" +
            "                                     |___|          \n" +
            " Environment: %s\n" +
            " Version: %s %s\n" +
            " Max heap: %d\n";

    protected final Logger log;

    private GuiceResteasyBootstrapServletContextListener resteasyBootstrap;
    private EnvironmentLoaderListener shiroEnvironmentLoaderListener;

    public NcmsBoot() {
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    protected String getLogo(WBConfiguration cfg) {
        //log.info(getLogo(cfg), cfg.getEnvironmentType(), cfg.getAppVersion(), Runtime.getRuntime().maxMemory());
        return String.format(LOGO,
                             cfg.getEnvironmentType(),
                             cfg.getAppVersion(),
                             cfg.getCoreProperties().getProperty("commit.hash", ""),
                             Runtime.getRuntime().maxMemory());
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ServletContext sctx = event.getServletContext();
        sctx.setInitParameter("WEBOOT_CFG_CLASS", NcmsEnvironment.class.getName());
        sctx.setInitParameter("shiroEnvironmentClass", NcmsShiroWebEnvironment.class.getName());
        sctx.setInitParameter("resteasy.document.expand.entity.references", "false");
        sctx.setInitParameter("resteasy.role.based.security", "true");

        super.contextInitialized(event);

        NcmsEnvironment env = (NcmsEnvironment) sctx.getAttribute(WEBOOT_CFG_SCTX_KEY);
        resteasyBootstrap = getInjector().getInstance(GuiceResteasyBootstrapServletContextListener.class);
        resteasyBootstrap.contextInitialized(event);

        initBeforeFilters(env, sctx);
        initCacheHeadersFilters(env, sctx);

        sctx.addFilter("ncmsGuardFilter", new NcmsGuardFilter(env))
            .addMappingForUrlPatterns(null,
                                      false, env.getAppPrefix() + "/*");
        sctx.addFilter("charsetFilter", CharsetFilter.class)
            .addMappingForUrlPatterns(null,
                                      false, env.getAppPrefix() + "/*");

        FilterRegistration.Dynamic shiroFilter = sctx.addFilter("shiroFilter", ShiroFilter.class);
        shiroFilter.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST,
                           DispatcherType.FORWARD,
                           DispatcherType.INCLUDE,
                           DispatcherType.ERROR),
                false, env.getAppPrefix() + "/*");

        // todo review it
        FilterRegistration.Dynamic gzipFilter = sctx.addFilter("gzipFilter", GzipFilter.class);
        gzipFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*.js");
        gzipFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*.css");

        initJarResources(env, sctx);
        initDirResources(env, sctx);
        initGuiceFilter(env, sctx);

        start();

        initAtmosphereServlet(env, sctx);

        log.info("Intialize SHIRO environment");
        shiroEnvironmentLoaderListener = new EnvironmentLoaderListener();
        shiroEnvironmentLoaderListener.contextInitialized(event);
    }

    protected void initBeforeFilters(NcmsEnvironment env, ServletContext sctx) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        stop();
        if (resteasyBootstrap != null) {
            resteasyBootstrap.contextDestroyed(servletContextEvent);
            resteasyBootstrap = null;
        }
        if (shiroEnvironmentLoaderListener != null) {
            shiroEnvironmentLoaderListener.contextDestroyed(servletContextEvent);
        }
        super.contextDestroyed(servletContextEvent);
    }

    protected void initGuiceFilter(NcmsEnvironment env, ServletContext sctx) {
        sctx.addFilter("guiceFilter", GuiceFilter.class)
            .addMappingForUrlPatterns(
                    EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD),
                    false, env.getAppPrefix() + "/*");

    }

    protected void initAtmosphereServlet(NcmsEnvironment env, ServletContext sctx) {
        sctx.addListener(SessionSupport.class);
        String apkgs = "com.softmotions.ncms,com.softmotions.ncms.atm," + getClass().getPackage().getName();
        ServletRegistration.Dynamic sreg = sctx.addServlet("AtmosphereServlet", AtmosphereServlet.class);
        sreg.setInitParameter(ApplicationConfig.OBJECT_FACTORY, NcmsAtmosphereObjectFactory.class.getName());
        sreg.setInitParameter(ApplicationConfig.ANNOTATION_PACKAGE, apkgs);
        sreg.setInitParameter(ApplicationConfig.CUSTOM_ANNOTATION_PACKAGE, apkgs);
        sreg.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT_SERVLET3, "true");
        sreg.setInitParameter(ApplicationConfig.PROPERTY_SESSION_SUPPORT, "false");
        sreg.setInitParameter(ApplicationConfig.ANALYTICS, "false");
        sreg.setInitParameter(ApplicationConfig.ATMOSPHERE_INTERCEPTORS, ShiroInterceptor.class.getName());
        sreg.setInitParameter(ApplicationConfig.HEARTBEAT_INTERVAL_IN_SECONDS, "30");
        sreg.setInitParameter(ApplicationConfig.BROADCASTER_ASYNC_WRITE_THREADPOOL_MAXSIZE, "50");
        sreg.setInitParameter(ApplicationConfig.BROADCASTER_MESSAGE_PROCESSING_THREADPOOL_MAXSIZE, "50");
        sreg.setInitParameter(ApplicationConfig.SCHEDULER_THREADPOOL_MAXSIZE, "4");
        sreg.setAsyncSupported(true);
        String mount = env.getAppPrefix() + "/ws/*";
        sreg.addMapping(mount);
        sreg.setLoadOnStartup(0);
        log.info("Atmosphere serving on {}", mount);

        Map<String, String> pmap = sreg.getInitParameters();
        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("line.separator");
        for (Map.Entry<String, String> e : pmap.entrySet()) {
            sb.append(sep).append(e.getKey()).append(" => ").append(e.getValue());
        }
        log.info("Atmosphere servlet settings: {}", sb);
    }

    @Override
    protected Collection<Module> getStartupModules() {
        List<Module> mlist = new ArrayList<>(1);
        mlist.add(new NcmsCoreModule());
        return mlist;
    }
}
