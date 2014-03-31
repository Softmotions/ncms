package com.softmotions.ncms;

import ninja.servlet.NinjaServletDispatcher;
import ninja.utils.NinjaProperties;
import com.softmotions.commons.web.JarResourcesProvider;
import com.softmotions.commons.web.JarResourcesServlet;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NCMS engine servlet module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletModule extends ServletModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsServletModule.class);

    protected void configureServlets() {
        log.info("Configuring NCMS modules and servlets");
        NinjaProperties nprops =
                (NinjaProperties) getServletContext()
                        .getAttribute(NcmsServletListener.NCMS_NINJA_PROPS_SCTX_KEY);
        if (nprops == null) {
            throw new RuntimeException("Unable to find Ninja framework properties in " +
                                       "ServletContext#" + NcmsServletListener.NCMS_NINJA_PROPS_SCTX_KEY
                                       + " attribute");
        }
        String ncmsCfgFile = nprops.get("ncms.configurationFile");
        if (ncmsCfgFile == null) {
            log.warn("Missing 'ncms.configurationFile' property in the ninja configuration, " +
                     "using fallback resource location: " + NcmsConfiguration.DEFAULT_CFG_RESOURCE);
        }

        //Bind NcmsConfiguration
        NcmsConfiguration cfg = new NcmsConfiguration(nprops, ncmsCfgFile, true);
        XMLConfiguration xcfg = cfg.impl();

        bind(NcmsConfiguration.class).toInstance(cfg);

        if (xcfg.configurationAt("mybatis") != null) {
            install(new NcmsMyBatisModule(cfg));
        }

        if (xcfg.configurationAt("liquibase") != null) {
            install(new NcmsLiquibaseModule());
        }

        initServlets(cfg);
    }

    protected void initServlets(NcmsConfiguration cfg) {
        //Ninja init part
        bind(NinjaServletDispatcher.class).asEagerSingleton();
        serve(cfg.getNcmsPrefix() + "/exec/*", NinjaServletDispatcher.class);
        serve(cfg.getNcmsPrefix() + "/admin/exec/*", NinjaServletDispatcher.class);

        initJarResourcesServlet(cfg);
    }

    protected void initJarResourcesServlet(NcmsConfiguration cfg) {
        Map<String, String> params = new LinkedHashMap<>();
        List<HierarchicalConfiguration> rlist = cfg.impl().configurationsAt("jar-web-resources.resource");
        for (HierarchicalConfiguration rcfg : rlist) {
            String pp = rcfg.getString("path-prefix");
            String opts = rcfg.getString("options");
            if (pp == null || opts == null) {
                continue;
            }
            params.put(pp, opts);
        }
        bind(JarResourcesServlet.class).in(Singleton.class);
        bind(JarResourcesProvider.class).to(JarResourcesServlet.class);
        serve(cfg.getNcmsPrefix() + "/*", JarResourcesServlet.class, params);

    }

    protected void serve(String pattern, Class<? extends HttpServlet> servletClass) {
        log.info("Serving {} with {}", pattern, servletClass);
        serve(pattern).with(servletClass);
    }

    protected void serve(String pattern, Class<? extends HttpServlet> servletClass, Map<String, String> params) {
        log.info("Serving {} with {}", pattern, servletClass);
        serve(pattern).with(servletClass, params);
    }

}
