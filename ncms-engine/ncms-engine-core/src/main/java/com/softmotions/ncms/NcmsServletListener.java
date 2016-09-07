package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.softmotions.ncms.shiro.NcmsShiroWebEnvironment;
import com.softmotions.ncms.utils.GzipFilter;
import com.softmotions.web.CharsetFilter;
import com.softmotions.weboot.WBServletListener;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletListener extends WBServletListener {

    protected final Logger log;

    private GuiceResteasyBootstrapServletContextListener resteasyBootstrap;
    private EnvironmentLoaderListener shiroEnvironmentLoaderListener;

    public NcmsServletListener() {
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    protected String getLogo() {
        return "";
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

        sctx.addFilter("guiceFilter", GuiceFilter.class)
            .addMappingForUrlPatterns(
                    EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD),
                    false, env.getAppPrefix() + "/*");

        start();

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

    @Override
    protected Collection<Module> getStartupModules() {
        List<Module> mlist = new ArrayList<>(1);
        mlist.add(new NcmsServletModule());
        return mlist;
    }
}
