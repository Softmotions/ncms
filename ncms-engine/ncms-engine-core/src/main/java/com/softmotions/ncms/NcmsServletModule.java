package com.softmotions.ncms;

import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.ncms.asm.render.AsmServlet;
import com.softmotions.ncms.jaxrs.NcmsJsonNodeReader;
import com.softmotions.ncms.jaxrs.NcmsRSExceptionHandler;
import com.softmotions.weboot.WBServletModule;

import com.google.inject.Singleton;

import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsServletModule extends WBServletModule<NcmsEnvironment> {

    protected void init(NcmsEnvironment env) {
        bind(NcmsEnvironment.class).toInstance(env);
        initJAXRS(env);
        initAsmServlet(env);
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

    protected Class<? extends AsmServlet> getAsmServletClass() {
        return AsmServlet.class;
    }
}
