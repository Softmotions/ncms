package ru.nsu.pagepdf;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.weboot.lifecycle.Dispose;
import com.softmotions.weboot.lifecycle.Start;

import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Singleton
public class HtmlToPdfModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(HtmlToPdfModule.class);

    protected void configure() {
        bind(HtmlToPdfModuleInitializer.class).asEagerSingleton();
    }

    public static class HtmlToPdfModuleInitializer {

        private final AsmDAO adao;

        private final PagePdfRS datars;

        private final NcmsEventBus ebus;

        private final Configuration cfg;

        private final ExecutorService saver;

        private final String[] cmdargstmpl;

        @Inject
        public HtmlToPdfModuleInitializer(NcmsEnvironment env, AsmDAO adao, PagePdfRS datars, NcmsEventBus ebus) {
            this.adao = adao;
            this.datars = datars;
            this.ebus = ebus;

            this.saver = Executors.newSingleThreadExecutor();
            this.cfg = env.xcfg().configurationAt("html-to-pdf");

            List<String> cmdargs = new ArrayList<>();
            cmdargs.add(cfg.getString("exec-path"));
            String extraParamsStr = cfg.getString("exec-extra-params");
            String[] extraParams = StringUtils.trimToEmpty(extraParamsStr).split("(\\s*\\n+\\s*)+");
            if (extraParams.length > 0) {
                Collections.addAll(cmdargs, extraParams);
            }
            // additional params for input params
            cmdargs.add("");
            cmdargs.add("");

            this.cmdargstmpl = cmdargs.toArray(new String[cmdargs.size()]);
        }

        @Start
        public void init() {
            ebus.register(this);
        }

        @Dispose
        public void shutdown() {
            try {
                saver.shutdownNow();
            } catch (Exception e) {
                log.error("", e);
            }
        }

        @Subscribe
        public void onAsmModified(final AsmModifiedEvent event) {
            saver.execute(() -> {
                Asm asm = null;
                String[] templates = cfg.getStringArray("asm-templates");
                if (templates == null || templates.length == 0) {
                    asm = adao.asmSelectById(event.getId());
                } else {
                    asm = adao.asmPlainByIdWithTemplates(event.getId(), templates);
                }

                if (asm != null && asm.isPublished()) {
                    try {
                        datars.removePagePdf(asm.getId());

                        File tmpFile = File.createTempFile("html2pdf", String.valueOf(asm.getId()));
                        tmpFile.deleteOnExit();
                        String[] cmdargs = cmdargstmpl.clone();
                        cmdargs[cmdargs.length - 2] = cfg.getString("page-url-template").replace("{id}", String.valueOf(asm.getId()));
                        cmdargs[cmdargs.length - 1] = tmpFile.getAbsolutePath();
                        Process wkhtmltopdf = Runtime.getRuntime().exec(cmdargs);
                        if (wkhtmltopdf.waitFor(1, TimeUnit.MINUTES)) {
                            try (final FileInputStream fis = new FileInputStream(tmpFile)) {
                                datars.savePagePdf(asm.getId(), fis);
                            }
                            log.info("Fetched PDF from " + cmdargs[cmdargs.length - 2]);
                        }
                    } catch (InterruptedException ignored) {
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                log.info("PDF Fetcher terminated");
            });
        }

        @Subscribe
        public void onAsmRemoved(final AsmRemovedEvent event) {
            saver.execute(() -> datars.removePagePdf(event.getId()));
        }
    }
}
