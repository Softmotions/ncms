package com.softmotions.ncms;

import ninja.servlet.NinjaServletDispatcher;
import ninja.utils.NinjaProperties;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.weboot.WBServletModule;
import com.softmotions.ncms.asm.render.AsmServlet;
import com.softmotions.ncms.jaxrs.NcmsRSExceptionHandler;
import com.softmotions.web.JarResourcesProvider;
import com.softmotions.web.JarResourcesServlet;

import com.google.inject.Singleton;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsServletModule extends WBServletModule<NcmsConfiguration> {

    protected NcmsConfiguration createConfiguration(NinjaProperties nprops) {
        String ncmsCfgFile = nprops.get("ncms.configurationFile");
        if (ncmsCfgFile == null) {
            log.warn("Missing 'ncms.configurationFile' property in the ninja configuration, " +
                     "using fallback resource location: " + NcmsConfiguration.DEFAULT_CFG_RESOURCE);
            ncmsCfgFile = NcmsConfiguration.DEFAULT_CFG_RESOURCE;
        }
        return new NcmsConfiguration(nprops, ncmsCfgFile, true);
    }

    protected void init(NcmsConfiguration cfg) {
        bind(NcmsConfiguration.class).toInstance(cfg);
        initNinjaDispatcher(cfg);
        initAsmServlet(cfg);
        initJAXRS(cfg);
        initJarResourcesServlet(cfg);
    }

    protected void initAsmServlet(NcmsConfiguration cfg) {
        //Assembly rendering servlet
        serve(cfg.getNcmsPrefix() + "/asm/*", AsmServlet.class);
    }

    protected void initNinjaDispatcher(NcmsConfiguration cfg) {
        //Ninja staff
        bind(NinjaServletDispatcher.class).in(Singleton.class);
        serve(cfg.getNcmsPrefix() + "/nj/*", NinjaServletDispatcher.class);
    }

    protected void initJAXRS(NcmsConfiguration cfg) {
        //Resteasy staff
        bind(NcmsRSExceptionHandler.class).in(Singleton.class);
        bind(HttpServletDispatcher.class).in(Singleton.class);
        serve(cfg.getNcmsPrefix() + "/rs/*",
              HttpServletDispatcher.class,
              new TinyParamMap().param("resteasy.servlet.mapping.prefix", cfg.getNcmsPrefix() + "/rs"));

        //Resteasy JS API
        bind(JSAPIServlet.class).in(Singleton.class);
        serve(cfg.getNcmsPrefix() + "/rjs", JSAPIServlet.class);
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
}
