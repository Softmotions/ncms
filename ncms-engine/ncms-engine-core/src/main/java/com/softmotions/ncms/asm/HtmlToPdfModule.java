package com.softmotions.ncms.asm;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.weboot.lifecycle.Start;

import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        private final GeneralDataRS datars;

        private final NcmsEventBus ebus;

        private final Configuration cfg;

        private final ExecutorService saver;

        @Inject
        public HtmlToPdfModuleInitializer(NcmsEnvironment env, AsmDAO adao, GeneralDataRS datars, NcmsEventBus ebus) {
            this.adao = adao;
            this.datars = datars;
            this.ebus = ebus;

            this.saver = Executors.newSingleThreadExecutor();
            this.cfg = env.xcfg().configurationAt("html-to-pdf");
        }

        @Start
        public void init() {
            ebus.register(this);

        }

        @Subscribe
        public void onAsmModified(final AsmModifiedEvent event) {
            saver.execute(() -> {
                // todo: filter by templates
                Asm asm = adao.asmSelectById(event.getId());

                if (asm != null && asm.isPublished()) {
                    try {
                        File tmpFile = File.createTempFile("html2pdf", String.valueOf(asm.getId()));
                        tmpFile.deleteOnExit();
                        Process wkhtmltopdf = Runtime.getRuntime()
                                .exec(
                                        new String[]{
                                                cfg.getString("exec-path"),
                                                "--print-media-type",
                                                cfg.getString("page-url-template").replace("{id}", String.valueOf(asm.getId())),
                                                tmpFile.getAbsolutePath()
                                        });

                        if (wkhtmltopdf.waitFor() == 0) {
                            try (final FileInputStream fis = new FileInputStream(tmpFile)) {
                                datars.savePagePdf(asm.getId(), IOUtils.toByteArray(fis));
                            }
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }

        @Subscribe
        public void onAsmRemoved(final AsmRemovedEvent event) {
            saver.execute(() -> datars.removePagePdf(event.getId()));
        }
    }
}
