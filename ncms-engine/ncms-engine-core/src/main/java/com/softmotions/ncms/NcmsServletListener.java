package com.softmotions.ncms;

import com.softmotions.web.CharsetFilter;
import com.softmotions.web.JarResourcesFilter;
import com.softmotions.web.security.SecurityFakeEnvFilter;
import com.softmotions.weboot.WBServletListener;

import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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
        sctx.setInitParameter("WEBOOT_CFG_CLASS", NcmsEnvironment.class.getName());

        Logger.setLoggerType(Logger.LoggerType.SLF4J);
        sctx.setInitParameter("resteasy.document.expand.entity.references", "false");
        sctx.setInitParameter("resteasy.role.based.security", "true");

        super.contextInitialized(event);

        NcmsEnvironment env = (NcmsEnvironment) sctx.getAttribute(WEBOOT_CFG_SCTX_KEY);
        resteasyBootstrap = getInjector().getInstance(GuiceResteasyBootstrapServletContextListener.class);
        resteasyBootstrap.contextInitialized(event);

        initJarResources(env, sctx);

        sctx.addFilter("charsetFilter", CharsetFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");

        initSecurity(env, sctx);

        sctx.addFilter("guiceFilter", GuiceFilter.class)
                .addMappingForUrlPatterns(null, false, "/*");

        start();

        for (Map.Entry<String, ? extends FilterRegistration> e : sctx.getFilterRegistrations().entrySet()) {
            FilterRegistration sreg = e.getValue();
            for (String m : sreg.getUrlPatternMappings()) {
                log.info(m + " => " + sreg.getName() + " (" + sreg.getClassName() + ")");
            }
        }
        for (Map.Entry<String, ? extends ServletRegistration> e : sctx.getServletRegistrations().entrySet()) {
            ServletRegistration sreg = e.getValue();
            for (String m : sreg.getMappings()) {
                log.info(m + " => " + sreg.getName() + " (" + sreg.getClassName() + ")");
            }
        }
        log.info(LOGO, env.getNcmsVersion());
    }


    private void initSecurity(NcmsEnvironment env, ServletContext sctx) {
        String webFakeUser = env.xcfg().getString("security.web-fakeuser");
        if (webFakeUser == null) {
            return;
        }
        String dbJndiName = env.xcfg().getString("security[@dbJndiName]");
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


    protected void initJarResources(NcmsEnvironment env, ServletContext sctx) {
        FilterRegistration.Dynamic fr = sctx.addFilter("jarResourcesFilter", JarResourcesFilter.class);
        fr.addMappingForUrlPatterns(null, false, env.getNcmsPrefix() + "/*");
        List<HierarchicalConfiguration> rlist = env.xcfg().configurationsAt("jar-web-resources.resource");
        for (HierarchicalConfiguration rcfg : rlist) {
            String pp = rcfg.getString("path-prefix");
            String opts = rcfg.getString("options");
            if (pp == null || opts == null) {
                continue;
            }
            fr.setInitParameter(pp, opts);
        }
        fr.setInitParameter("strip-prefix", env.getNcmsPrefix());
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
