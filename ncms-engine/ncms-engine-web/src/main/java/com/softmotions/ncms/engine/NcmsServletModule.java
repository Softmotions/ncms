package com.softmotions.ncms.engine;

import com.softmotions.commons.web.JarResourcesServlet;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

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
        String aprefix = "/" + NcmsConfiguration.URL_ACCESS_PREFIX;

        bind(AssemblyServlet.class).in(Singleton.class);
        serveWithServletClass(aprefix + "/asm", AssemblyServlet.class);

        //It should be the last in mapping
        bind(JarResourcesServlet.class).in(Singleton.class);
        serveWithServletClass(aprefix + "/*", JarResourcesServlet.class);
    }

    private void serveWithServletClass(String pattern, Class<? extends HttpServlet> clazz) {
        log.info("SERVE {} => {}", pattern, clazz.getName());
        serve(pattern).with(clazz);
    }
}
