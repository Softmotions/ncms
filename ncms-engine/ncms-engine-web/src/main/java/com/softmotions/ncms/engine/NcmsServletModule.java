package com.softmotions.ncms.engine;

import com.softmotions.commons.web.JarResourcesProvider;
import com.softmotions.commons.web.JarResourcesServlet;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ncms servlet module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsServletModule extends ServletModule {

    private static final Logger log = LoggerFactory.getLogger(NcmsServletModule.class);

    private final NcmsConfiguration cfg;

    public NcmsServletModule(NcmsConfiguration cfg) {
        this.cfg = cfg;
    }

    protected void configureServlets() {
        log.info("CONFIGURING NCMS SERVLETS:");
        String aprefix = cfg.getUrlPrefix();

        bind(AssemblyServlet.class).in(Singleton.class);
        serveWithServletClass(aprefix + "/asm", AssemblyServlet.class);

        //It should be the last in mapping
        registerJarResourcesServlet();
    }

    private void registerJarResourcesServlet() {
        String aprefix = cfg.getUrlPrefix();
        Map<String, String> params = new LinkedHashMap<>();
        List<HierarchicalConfiguration> rlist = cfg.impl().configurationsAt("jar-web-resources.resource");
        for (HierarchicalConfiguration rcfg : rlist) {
            String pp = rcfg.getString("path-prefix");
            String opts = rcfg.getString("options");
            if (pp == null || opts == null) {
                log.warn("Skipping invalid configuration: " + rcfg);
                continue;
            }
            params.put(pp, opts);
        }
        bind(JarResourcesServlet.class).in(Singleton.class);
        bind(JarResourcesProvider.class).to(JarResourcesServlet.class);
        serveWithServletClass(aprefix + "/*", JarResourcesServlet.class, params);
    }

    private void serveWithServletClass(String pattern, Class<? extends HttpServlet> clazz) {
        serveWithServletClass(pattern, clazz, null);
    }

    private void serveWithServletClass(String pattern, Class<? extends HttpServlet> clazz, Map<String, String> initParams) {
        log.info("SERVE {} => {}", pattern, clazz.getName());
        if (initParams == null) {
            serve(pattern).with(clazz);
        } else {
            serve(pattern).with(clazz, initParams);
        }
    }
}
