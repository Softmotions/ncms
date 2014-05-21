package com.softmotions.ncms.asm;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.asm.render.AsmAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmRefAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmResourceAttributeRenderer;
import com.softmotions.ncms.asm.render.AsmResourceLoader;
import com.softmotions.ncms.asm.render.AsmStringAttributeRenderer;
import com.softmotions.ncms.asm.render.DefaultAsmRenderer;
import com.softmotions.ncms.asm.render.ldrs.AsmClasspathResourceLoader;
import com.softmotions.ncms.media.MediaResource;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(AsmModule.class);

    protected void configure() {
        bind(AsmDAO.class);
        bind(AsmRenderer.class).to(DefaultAsmRenderer.class);

        Multibinder<AsmAttributeRenderer> attrBinder =
                Multibinder.newSetBinder(binder(), AsmAttributeRenderer.class);
        attrBinder.addBinding().to(AsmStringAttributeRenderer.class);
        attrBinder.addBinding().to(AsmRefAttributeRenderer.class);
        attrBinder.addBinding().to(AsmResourceAttributeRenderer.class);

        //Resource loader
        bind(AsmResourceLoader.class).to(AsmResourceLoaderImpl.class).in(Singleton.class);

        //Asm REST
        bind(AsmRS.class).in(Singleton.class);
    }


    public static class AsmResourceLoaderImpl implements AsmResourceLoader {

        final AsmResourceLoader[] loaders;

        @Inject
        public AsmResourceLoaderImpl(NcmsConfiguration cfg, Injector injector) throws Exception {
            XMLConfiguration xcfg = cfg.impl();
            List<AsmResourceLoader> ldrs = new ArrayList<>();
            List<HierarchicalConfiguration> hcl = xcfg.configurationsAt("asm.resource-loaders");
            ClassLoader cl = ObjectUtils.firstNonNull(
                    Thread.currentThread().getContextClassLoader(),
                    getClass().getClassLoader()
            );
            for (HierarchicalConfiguration hc : hcl) {
                String className = hc.getString("loader[@class]");
                if (StringUtils.isBlank(className)) {
                    continue;
                }
                AsmResourceLoader ldr = (AsmResourceLoader)
                        injector.getInstance(cl.loadClass(className));
                log.info("Register resource loader: " + className);
                ldrs.add(ldr);
            }
            if (ldrs.isEmpty()) {
                log.warn("No resource loaders configured " +
                         "using fallback loader: " + AsmClasspathResourceLoader.class);
                ldrs.add(injector.getInstance(AsmClasspathResourceLoader.class));
            }
            loaders = ldrs.toArray(new AsmResourceLoader[ldrs.size()]);
        }

        public boolean exists(String name, Locale locale) {
            if (loaders.length == 1) {
                return loaders[0].exists(name, locale);
            }
            for (final AsmResourceLoader l : loaders) {
                if (l.exists(name, locale)) {
                    return true;
                }
            }
            return false;
        }

        public MediaResource load(String name, Locale locale) throws IOException {
            if (loaders.length == 1) {
                return loaders[0].load(name, locale);
            }
            MediaResource res = null;
            for (final AsmResourceLoader l : loaders) {
                try {
                    res = l.load(name, locale);
                } catch (IOException e) {
                    ;
                }
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
    }
}
