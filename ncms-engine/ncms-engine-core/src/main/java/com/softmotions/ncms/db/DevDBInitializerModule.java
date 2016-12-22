package com.softmotions.ncms.db;

import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;
import com.softmotions.ncms.asm.AsmDAO;

/**
 * Initiates development database with initial data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class DevDBInitializerModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(DevDBInitializerModule.class);

    @Override
    protected void configure() {
        bind(DevDBInitializer.class).asEagerSingleton();
    }

    public static class DevDBInitializer {

        @Inject
        AsmDAO adao;

        @Start(order = 50)
        @Transactional
        public void init() throws Exception {
            log.info("Initializing development database with test data...");

            Asm asm = adao.asmSelectByName("base");
            if (asm == null) {
                asm = new Asm("base");
                asm.addAttribute(new AsmAttribute("title", "Заголовок", "string", "Hello world"));
                asm.addAttribute(new AsmAttribute("copyright", "Copyright", "string", "My company (c)"));
                adao.asmInsert(asm);
            }
            asm = adao.asmSelectByName("main");
            if (asm == null) {
                asm = new Asm("main", new AsmCore("foo/bar", "fobarcore"));
                adao.asmInsert(asm);
                adao.asmSetParent(asm, adao.asmSelectByName("base"));
            }
            asm = adao.asmSelectByName("content");
            if (asm == null) {
                asm = new Asm("content");
                asm.setDescription("Основная страница портала");
                asm.setTemplate(true);
                asm.addAttribute(new AsmAttribute("content", "Контент", "string", "Simple text"));
                adao.asmInsert(asm);
                adao.asmSetParent(asm, adao.asmSelectByName("main"));
            }
        }
    }
}
