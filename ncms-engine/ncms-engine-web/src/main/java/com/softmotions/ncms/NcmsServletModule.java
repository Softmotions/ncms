package com.softmotions.ncms;

import ninja.servlet.NinjaServletDispatcher;
import ninja.utils.NinjaProperties;
import com.softmotions.commons.web.JarResourcesProvider;
import com.softmotions.commons.web.JarResourcesServlet;
import com.softmotions.commons.weboot.WBServletModule;
import com.softmotions.ncms.adm.AdmServlet;
import com.softmotions.ncms.asm.render.AsmServlet;

import com.google.inject.Singleton;

import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
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
        bind(NinjaServletDispatcher.class).asEagerSingleton();
        serve(cfg.getNcmsPrefix() + "/nj/*", NinjaServletDispatcher.class);
        serve(cfg.getNcmsPrefix() + "/asm/*", AsmServlet.class);
        serve(cfg.getNcmsPrefix() + "/adm/*", AdmServlet.class);
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
}
