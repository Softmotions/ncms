package com.softmotions.ncms.asm.render.httl;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.softmotions.commons.lifecycle.Dispose;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.asm.render.AsmTemplateEngineAdapter;
import com.softmotions.ncms.atm.ServerMessageEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.events.MediaUpdateEvent;
import com.softmotions.weboot.executor.TaskExecutor;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmTemplateEngineHttlModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(HttlTemplateCheckService.class);

    @Override
    protected void configure() {
        Multibinder<AsmTemplateEngineAdapter> teBinder =
                Multibinder.newSetBinder(binder(), AsmTemplateEngineAdapter.class);
        teBinder.addBinding().to(AsmTemplateEngineHttlAdapter.class);
        bind(HttlTemplateCheckService.class).asEagerSingleton();
    }


    public static class HttlTemplateCheckService {

        private final AsmTemplateEngineAdapter adapter;

        private final NcmsEventBus ebus;

        private final TaskExecutor executor;

        private final String[] extensions;

        @Inject
        HttlTemplateCheckService(Set<AsmTemplateEngineAdapter> engines,
                                 NcmsEventBus ebus,
                                 TaskExecutor executor) {
            this.executor = executor;
            this.ebus = ebus;
            AsmTemplateEngineAdapter found = null;
            for (AsmTemplateEngineAdapter e : engines) {
                if ("httl".equals(e.getType())) {
                    found = e;
                    break;
                }
            }
            adapter = found;
            if (found != null) {
                extensions = found.getSupportedExtensions();
            } else {
                extensions = ArrayUtils.EMPTY_STRING_ARRAY;
            }
        }

        @Subscribe
        public void mediaUpdated(MediaUpdateEvent ev) {
            if (adapter == null) {
                return;
            }
            String path = ev.getPath();
            if (ev.isFolder() || ev.hints().get("app") == null) {
                return;
            }
            String ext = FilenameUtils.getExtension(path).toLowerCase();
            if (ArrayUtils.indexOf(extensions, ext) == -1) {
                return;
            }
            executor.submit(new HttlTemplateCheckTask(ev, this));
        }

        @Start
        public void start() {
            ebus.register(this);
        }

        @Dispose
        public void shutdown() {
            ebus.unregister(this);
        }
    }

    private static class HttlTemplateCheckTask implements Runnable {

        private final HttlTemplateCheckService service;

        private final MediaUpdateEvent ev;

        private HttlTemplateCheckTask(MediaUpdateEvent ev,
                                      HttlTemplateCheckService service) {
            this.ev = ev;
            this.service = service;
        }

        void reportError(String msg) {
            ServerMessageEvent err = new ServerMessageEvent(this, msg, true, true, null);
            String app = (String) ev.hints().get("app");
            if (app != null) {
                err.hint("app", app);
            }
            service.ebus.fire(err);
        }

        @Override
        public void run() {
            log.info("Checking a syntax of template: {}", ev.getPath());
            String path = ev.getPath();
            try {
                service.adapter.checkTemplateSyntax(path);
            } catch (AsmTemplateSyntaxException e) {
                log.warn("Template syntax error: {} template: {}", e.getMessage(), path);
                reportError(e.getMessage());
            } catch (IOException e) {
                log.warn("IO error while checking syntax of the template: {} Error: {}", path, e.toString());
            }
        }
    }
}
