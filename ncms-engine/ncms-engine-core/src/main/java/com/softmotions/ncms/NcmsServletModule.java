package com.softmotions.ncms;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.ncms.asm.render.AsmFilter;
import com.softmotions.ncms.jaxrs.NcmsRSExceptionHandler;
import com.softmotions.ncms.jaxrs.ResteasyUTF8CharsetFilter;
import com.softmotions.ncms.utils.BrowserFilter;
import com.softmotions.weboot.WBServletModule;
import com.softmotions.weboot.jaxrs.WBJaxrsModule;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsServletModule extends WBServletModule<NcmsEnvironment> {

    @Override
    protected void init(NcmsEnvironment env) {
        bind(NcmsEnvironment.class).toInstance(env);
        bind(new TypeLiteral<HierarchicalConfiguration<ImmutableNode>>() {
        }).toInstance(env.xcfg());
        initBrowserFilter(env);
        initJAXRS(env);
        initAsmFilter(env);
    }

    protected void initAsmFilter(NcmsEnvironment env) {
        //Assembly rendering filter
        Class<? extends AsmFilter> clazz = getAsmFilterClass();
        String ncmsp = env.getAppPrefix();
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
        String appPrefix = env.getAppPrefix();
        //Resteasy staff
        install(new WBJaxrsModule());
        bind(NcmsRSExceptionHandler.class).in(Singleton.class);
        bind(ResteasyUTF8CharsetFilter.class).in(Singleton.class);
        bind(HttpServletDispatcher.class).in(Singleton.class);
        log.info("Resteasy serving on {}", appPrefix + "/rs/*");
        serve(appPrefix + "/rs/*")
                .with(HttpServletDispatcher.class,
                      new TinyParamMap().param("resteasy.servlet.mapping.prefix", appPrefix + "/rs"));

        //Resteasy JS API
        bind(JSAPIServlet.class).in(Singleton.class);
        serve(appPrefix + "/rjs")
                .with(JSAPIServlet.class);
    }

    protected Class<? extends AsmFilter> getAsmFilterClass() {
        return AsmFilter.class;
    }

    protected void initBrowserFilter(NcmsEnvironment env) {
        HierarchicalConfiguration<ImmutableNode> xcfg = env.xcfg();
        if (xcfg.configurationsAt("browser-filter").isEmpty()) {
            return;
        }
        String ncmsp = env.getAppPrefix();
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
