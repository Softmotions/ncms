package com.softmotions.ncms;

import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.ncms.asm.render.AsmFilter;
import com.softmotions.ncms.jaxrs.NcmsJsonNodeReader;
import com.softmotions.ncms.jaxrs.NcmsRSExceptionHandler;
import com.softmotions.ncms.utils.BrowserFilter;
import com.softmotions.weboot.WBServletModule;

import com.google.inject.Singleton;

import org.apache.commons.configuration.XMLConfiguration;
import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsServletModule extends WBServletModule<NcmsEnvironment> {

    protected void init(NcmsEnvironment env) {
        bind(NcmsEnvironment.class).toInstance(env);
        initBrowserFilter(env);
        initJAXRS(env);
        initAsmFilter(env);
    }

    protected void initAsmFilter(NcmsEnvironment env) {
        //Assembly rendering filter
        Class<? extends AsmFilter> clazz = getAsmFilterClass();
        String ncmsp = env.getNcmsPrefix();
        KVOptions opts = new KVOptions();
        opts.put("strip-prefixes", (ncmsp + "/asm,") + (ncmsp + "/adm/asm,") + (ncmsp.isEmpty() ? "/" : ncmsp));
        String[] exclude = env.xcfg().getStringArray("asm.exclude");
        if (exclude.length == 0) {
            exclude = new String[]{ncmsp + "/rs", ncmsp + "/rjs"};
        }
        opts.put("exclude-prefixes", ArrayUtils.stringJoin(exclude, ","));
        filter(ncmsp + "/*", clazz, opts);
    }

    protected void initJAXRS(NcmsEnvironment env) {
        String ncmsp = env.getNcmsPrefix();
        //Resteasy staff
        bind(NcmsRSExceptionHandler.class).in(Singleton.class);
        bind(NcmsJsonNodeReader.class).in(Singleton.class);
        bind(HttpServletDispatcher.class).in(Singleton.class);
        serve(ncmsp + "/rs/*")
                .with(HttpServletDispatcher.class,
                      new TinyParamMap().param("resteasy.servlet.mapping.prefix", ncmsp + "/rs"));

        //Resteasy JS API
        bind(JSAPIServlet.class).in(Singleton.class);
        serve(ncmsp + "/rjs")
                .with(JSAPIServlet.class);
    }

    protected Class<? extends AsmFilter> getAsmFilterClass() {
        return AsmFilter.class;
    }

    protected void initBrowserFilter(NcmsEnvironment env) {
        XMLConfiguration xcfg = env.xcfg();
        if (xcfg.configurationsAt("browser-filter").isEmpty()) {
            return;
        }

        String ncmsp = env.getNcmsPrefix();
        KVOptions opts = new KVOptions();
        opts.put("min-trident", String.valueOf(xcfg.getFloat("browser-filter.min-trident", 0)));
        String badUrl = xcfg.getString("browser-filter.bad-browser-uri", "");
        opts.put("redirect-uri", badUrl.isEmpty() ? null : ncmsp + badUrl);
        String[] exclude = xcfg.getStringArray("browser-filter.exclude");
        if (exclude.length == 0) {
            exclude = new String[]{ncmsp + "/rs", ncmsp + "/rjs"};
        }
        opts.put("exclude-prefixes", ArrayUtils.stringJoin(exclude, ","));
        filter(ncmsp + "/*", BrowserFilter.class, opts);
    }
}
