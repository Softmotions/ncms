package com.softmotions.ncms.engine;

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

    protected void configureServlets() {
        log.info("CONFIGURING NCMS SERVLETS:");
        bind(AssemblyServlet.class).in(Singleton.class);
        serveWithServletClass("/" + NcmsConstants.URL_ACCESS_PREFIX + "/asm", AssemblyServlet.class);
    }

    private void serveWithServletClass(String pattern, Class<? extends HttpServlet> clazz) {
        log.info("\tSERVE {} => {}", pattern, clazz.getName());
        serve(pattern).with(clazz);
    }
}
