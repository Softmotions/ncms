package com.softmotions.ncms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.CollectionUtils;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.adm.AdmModule;
import com.softmotions.ncms.asm.AsmModule;
import com.softmotions.ncms.asm.render.AsmFilter;
import com.softmotions.ncms.asm.render.httl.AsmTemplateEngineHttlModule;
import com.softmotions.ncms.events.EventsModule;
import com.softmotions.ncms.jaxrs.NcmsRSExceptionMapper;
import com.softmotions.ncms.media.MediaModule;
import com.softmotions.ncms.mediawiki.MediaWikiModule;
import com.softmotions.ncms.mtt.MttModule;
import com.softmotions.ncms.mtt.http.MttHttpFilter;
import com.softmotions.ncms.qa.QAModule;
import com.softmotions.ncms.rds.RefDataStoreModule;
import com.softmotions.ncms.security.NcmsSecurityModule;
import com.softmotions.ncms.update.UpdateModule;
import com.softmotions.ncms.user.UserModule;
import com.softmotions.ncms.utils.BrowserFilter;
import com.softmotions.weboot.WBServletModule;
import com.softmotions.weboot.executor.TaskExecutorModule;
import com.softmotions.weboot.i18n.I18nModule;
import com.softmotions.weboot.jaxrs.WBJaxrsModule;
import com.softmotions.weboot.liquibase.WBLiquibaseModule;
import com.softmotions.weboot.mb.WBMyBatisModule;
import com.softmotions.weboot.scheduler.SchedulerModule;
import com.softmotions.weboot.security.WBSecurityModule;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class NcmsModule extends WBServletModule<NcmsEnvironment> {

    @Override
    protected void init(NcmsEnvironment env) {
        bind(NcmsEnvironment.class).toInstance(env);
        bind(new TypeLiteral<HierarchicalConfiguration<ImmutableNode>>() {
        }).toInstance(env.xcfg());
        initMarketingToolsFilter(env);
        initBrowserFilter(env);
        initJAXRS(env);
        initAsmFilter(env);
        install(new ShiroAopModule());
        install(new I18nModule(env, "com.softmotions.ncms.Messages"));
        install(new WBMyBatisModule(env));
        install(new WBLiquibaseModule(env));
        install(new WBSecurityModule(env, "Softmotions"));
        install(new NcmsSecurityModule());
        install(new SchedulerModule(env));
        install(new TaskExecutorModule(env));
        install(new UpdateModule());
        install(new EventsModule());
        install(new AsmModule());
        install(new AsmTemplateEngineHttlModule());
        install(new AdmModule());
        install(new MediaModule());
        install(new MediaWikiModule(env));
        install(new UserModule());
        install(new RefDataStoreModule());
        install(new QAModule(env));
        install(new MttModule());
        install(new NcmsLogoModule());
    }

    protected void initAsmFilter(NcmsEnvironment env) {
        //Assembly rendering filter
        Class<? extends AsmFilter> clazz = getAsmFilterClass();
        String ncmsp = env.getAppPrefix();
        KVOptions opts = new KVOptions();
        opts.put("strip-prefixes", (ncmsp + "/asm,") + (ncmsp + "/adm/asm,") + (ncmsp.isEmpty() ? "/" : ncmsp));
        List<String> exclude = new ArrayList<>(Arrays.asList(env.xcfg().getStringArray("asm.exclude")));
        for (String e : new String[]{
                ncmsp + "/rs",
                ncmsp + "/rjs",
                ncmsp + "/ws"}) {
            if (!exclude.contains(e)) {
                exclude.add(e);
            }
        }
        opts.put("exclude-prefixes", CollectionUtils.join(",", exclude));
        filter(ncmsp + "/*", clazz, opts);
    }

    protected void initJAXRS(NcmsEnvironment env) {
        String ncmsp = env.getAppPrefix();

        //Resteasy staff
        install(new WBJaxrsModule(env));
        bind(NcmsRSExceptionMapper.class).in(Singleton.class);
        bind(HttpServletDispatcher.class).in(Singleton.class);
        String mount = ncmsp + "/rs/*";
        log.info("Resteasy serving on {}", mount);
        serve(mount)
                .with(HttpServletDispatcher.class,
                      new TinyParamMap()
                              .param("resteasy.servlet.mapping.prefix", ncmsp + "/rs"));

        //Resteasy JS API
        bind(JSAPIServlet.class).in(Singleton.class);
        serve(ncmsp + "/rjs")
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

    protected void initMarketingToolsFilter(NcmsEnvironment env) {
        filter(env.getAppPrefix() + "/*", MttHttpFilter.class);
    }

    /**
     * Display NCM logo after startup.
     *
     * @author Adamansky Anton (adamansky@gmail.com)
     */
    public static class NcmsLogoModule extends AbstractModule {

        private static final Logger log = LoggerFactory.getLogger(NcmsLogoModule.class);

        private static final String LOGO =
                "                                                    \n" +
                " _____ _____ _____ _____    _____         _         \n" +
                "|   | |     |     |   __|  |   __|___ ___|_|___ ___ \n" +
                "| | | |   --| | | |__   |  |   __|   | . | |   | -_|\n" +
                "|_|___|_____|_|_|_|_____|  |_____|_|_|_  |_|_|_|___|\n" +
                "                                     |___|          \n" +
                " Environment: %s\n" +
                " Version: %s\n" +
                " Max heap: %s\n";


        @Override
        protected void configure() {
            bind(LogoStarter.class).asEagerSingleton();
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        public static class LogoStarter {

            final NcmsEnvironment env;

            @Inject
            public LogoStarter(NcmsEnvironment env) {
                this.env = env;
            }

            @Start(order = Integer.MAX_VALUE)
            public void startup() {
                log.info(String.format(LOGO, env.getEnvironmentType(), env.getAppVersion(), Runtime.getRuntime().maxMemory()));
            }
        }
    }
}
