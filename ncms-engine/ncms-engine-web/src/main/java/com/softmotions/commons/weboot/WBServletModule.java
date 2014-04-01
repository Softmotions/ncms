package com.softmotions.commons.weboot;

import ninja.utils.NinjaProperties;

import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.List;
import java.util.Map;

/**
 * Weboot engine servlet module.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class WBServletModule<C extends WBConfiguration> extends ServletModule {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected void configureServlets() {
        log.info("Configuring WB modules and servlets");
        NinjaProperties nprops =
                (NinjaProperties) getServletContext()
                        .getAttribute(WBServletListener.WB_NINJA_PROPS_SCTX_KEY);
        if (nprops == null) {
            throw new RuntimeException("Unable to find Ninja framework properties in " +
                                       "ServletContext#" + WBServletListener.WB_NINJA_PROPS_SCTX_KEY
                                       + " attribute");
        }
        //Bind configuration
        C cfg = createConfiguration(nprops);
        XMLConfiguration xcfg = cfg.impl();
        bind(WBConfiguration.class).toInstance(cfg);

        if (xcfg.configurationAt("mybatis") != null) {
            install(new WBMyBatisModule(cfg));
        }
        if (xcfg.configurationAt("liquibase") != null) {
            install(new WBLiquibaseModule());
        }

        ClassLoader cl = ObjectUtils.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader()
        );
        List<HierarchicalConfiguration> mconfigs = xcfg.configurationsAt("modules.module");
        for (final HierarchicalConfiguration mcfg : mconfigs) {
            String mclassName = mcfg.getString("[@class]");
            if (StringUtils.isBlank(mclassName)) {
                continue;
            }
            try {
                Class mclass = cl.loadClass(mclassName);
                if (!Module.class.isAssignableFrom(mclass)) {
                    log.warn("Module class: " + mclassName + " is not Guice module, skipped");
                    continue;
                }
                log.info("Installing " + mclassName + " Guice module");
                install((Module) mclass.newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to activate Guice module: " + mclassName, e);
            }
        }
        init(cfg);
    }

    protected abstract C createConfiguration(NinjaProperties nprops);

    protected abstract void init(C cfg);

    protected void serve(String pattern, Class<? extends HttpServlet> servletClass) {
        log.info("Serving {} with {}", pattern, servletClass);
        serve(pattern).with(servletClass);
    }

    protected void serve(String pattern, Class<? extends HttpServlet> servletClass, Map<String, String> params) {
        log.info("Serving {} with {}", pattern, servletClass);
        serve(pattern).with(servletClass, params);
    }

}
