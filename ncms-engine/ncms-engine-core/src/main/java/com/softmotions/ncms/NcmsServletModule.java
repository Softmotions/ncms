package com.softmotions.ncms;

import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.ncms.asm.render.AsmServlet;
import com.softmotions.ncms.jaxrs.NcmsJsonNodeReader;
import com.softmotions.ncms.jaxrs.NcmsRSExceptionHandler;
import com.softmotions.web.JarResourcesProvider;
import com.softmotions.web.JarResourcesServlet;
import com.softmotions.weboot.WBServletModule;

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
public class NcmsServletModule extends WBServletModule<NcmsEnvironment> {

    protected void init(NcmsEnvironment env) {
        bind(NcmsEnvironment.class).toInstance(env);
        initBefore(env);
        initJAXRS(env);
        initAsmServlet(env);
        initJarResourcesServlet(env);
        initAfter(env);
    }

    protected void initAsmServlet(NcmsEnvironment env) {
        //Assembly rendering servlet
        Class<? extends AsmServlet> clazz = getAsmServletClass();
        serve(env.getNcmsPrefix() + "/asm/*", clazz);
        serve(env.getNcmsPrefix() + "/adm/asm/*", clazz);
    }

    protected void initJAXRS(NcmsEnvironment env) {
        //Resteasy staff
        bind(NcmsRSExceptionHandler.class).in(Singleton.class);
        bind(NcmsJsonNodeReader.class).in(Singleton.class);
        bind(HttpServletDispatcher.class).in(Singleton.class);
        serve(env.getNcmsPrefix() + "/rs/*",
              HttpServletDispatcher.class,
              new TinyParamMap().param("resteasy.servlet.mapping.prefix", env.getNcmsPrefix() + "/rs"));

        //Resteasy JS API
        bind(JSAPIServlet.class).in(Singleton.class);
        serve(env.getNcmsPrefix() + "/rjs", JSAPIServlet.class);
    }

    protected void initJarResourcesServlet(NcmsEnvironment env) {
        Map<String, String> params = new LinkedHashMap<>();
        List<HierarchicalConfiguration> rlist = env.xcfg().configurationsAt("jar-web-resources.resource");
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
        serve(env.getNcmsPrefix() + "/*", JarResourcesServlet.class, params);
    }

    protected Class<? extends AsmServlet> getAsmServletClass() {
        return AsmServlet.class;
    }

    protected void initBefore(NcmsEnvironment env) {

    }

    protected void initAfter(NcmsEnvironment env) {

    }
}
