package com.softmotions.ncms.asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.am.*;
import com.softmotions.ncms.asm.render.AsmRenderer;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRendererContextFactory;
import com.softmotions.ncms.asm.render.AsmRendererContextImpl;
import com.softmotions.ncms.asm.render.AsmResourceLoader;
import com.softmotions.ncms.asm.render.DefaultAsmRenderer;
import com.softmotions.ncms.asm.render.ldrs.AsmClasspathResourceLoader;
import com.softmotions.ncms.asm.render.ldrs.AsmMediaServiceResourceLoader;
import com.softmotions.ncms.media.MediaResource;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(AsmModule.class);

    @Override
    protected void configure() {

        install(new FactoryModuleBuilder()
                        .implement(AsmRendererContext.class,
                                   AsmRendererContextImpl.class)
                        .build(AsmRendererContextFactory.class));

        Multibinder<AsmAttributeManager> attrBinder =
                Multibinder.newSetBinder(binder(), AsmAttributeManager.class);
        attrBinder.addBinding().to(AsmStringAM.class);
        attrBinder.addBinding().to(AsmRefAM.class);
        attrBinder.addBinding().to(AsmWebRefAM.class);
        attrBinder.addBinding().to(AsmSelectAM.class);
        attrBinder.addBinding().to(AsmBooleanAM.class);
        attrBinder.addBinding().to(AsmTreeAM.class);
        attrBinder.addBinding().to(AsmWikiAM.class);
        attrBinder.addBinding().to(AsmImageAM.class);
        attrBinder.addBinding().to(AsmFileRefAM.class);
        attrBinder.addBinding().to(AsmPageRefAM.class);
        attrBinder.addBinding().to(AsmBreadCrumbsAM.class);
        attrBinder.addBinding().to(AsmMainPageAM.class);
        attrBinder.addBinding().to(AsmRichRefAM.class);
        attrBinder.addBinding().to(AsmDateAM.class);
        attrBinder.addBinding().to(AsmBumpOrdinalAM.class);
        attrBinder.addBinding().to(AsmMedialineAM.class);
        attrBinder.addBinding().to(AsmAliasAM.class);
        attrBinder.addBinding().to(AsmCoreAM.class);
        attrBinder.addBinding().to(AsmTableAM.class);
        attrBinder.addBinding().to(AsmVisualEditorAM.class);

        //Resource loader
        bind(AsmResourceLoader.class).to(AsmResourceLoaderImpl.class).in(Singleton.class);
        bind(AsmEventsListener.class).asEagerSingleton();

        bind(AsmDAO.class);
        bind(AsmRenderer.class).to(DefaultAsmRenderer.class);

        bind(AsmAttributeManagersRegistry.class).to(DefaultAsmAttributeManagersRegistry.class).in(Singleton.class);
        bind(PageSecurityService.class).in(Singleton.class);
        bind(PageService.class).to(PageRS.class);

        bind(AsmRS.class).in(Singleton.class);
        bind(PageRS.class).in(Singleton.class);

        // Standalone RS services
        bind(AsmTreeAM.class).in(Singleton.class);
        bind(AsmVisualEditorAM.class).in(Singleton.class);
    }


    static class AsmResourceLoaderImpl implements AsmResourceLoader {

        final AsmResourceLoader[] loaders;

        @Inject
        AsmResourceLoaderImpl(NcmsEnvironment env, Injector injector) throws Exception {
            HierarchicalConfiguration<ImmutableNode> xcfg = env.xcfg();
            Set<String> ldrsClasses = new HashSet<>();
            List<AsmResourceLoader> ldrs = new ArrayList<>();
            List<HierarchicalConfiguration<ImmutableNode>> hcl = xcfg.configurationsAt("asm.resource-loaders");
            ClassLoader cl = ObjectUtils.firstNonNull(
                    Thread.currentThread().getContextClassLoader(),
                    getClass().getClassLoader()
            );
            for (HierarchicalConfiguration hc : hcl) {
                String className = hc.getString("loader");
                if (StringUtils.isBlank(className)) {
                    continue;
                }
                AsmResourceLoader ldr = (AsmResourceLoader)
                        injector.getInstance(cl.loadClass(className));
                log.info("Register resource loader: {}", className);
                ldrs.add(ldr);
                ldrsClasses.add(className);
            }
            // Always add AsmMediaServiceResourceLoader to the list of loaders
            if (!ldrsClasses.contains(AsmMediaServiceResourceLoader.class.getName())) {
                ldrs.add(injector.getInstance(AsmMediaServiceResourceLoader.class));
            }
            if (ldrs.isEmpty()) {
                log.warn("No resource loaders configured using fallback loader: {}", AsmClasspathResourceLoader.class);
                ldrs.add(injector.getInstance(AsmClasspathResourceLoader.class));
            }
            loaders = ldrs.toArray(new AsmResourceLoader[ldrs.size()]);
        }

        @Override
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

        @Override
        public MediaResource load(String name, Locale locale) throws IOException {
            if (loaders.length == 1) {
                return loaders[0].load(name, locale);
            }
            MediaResource res = null;
            for (final AsmResourceLoader l : loaders) {
                try {
                    res = l.load(name, locale);
                } catch (IOException ignored) {
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
