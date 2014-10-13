package com.softmotions.ncms.qa;

import it.sauronsoftware.cron4j.Scheduler;
import com.softmotions.ncms.NcmsEnvironment;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.util.Set;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/pages/qa")
@Singleton
public class PageQARS {

    private static final Logger log = LoggerFactory.getLogger(PageQARS.class);

    private final Set<PageQAPlugin> plugins;

    private final Scheduler scheduler;

    private final NcmsEnvironment env;

    @Inject
    public PageQARS(Set<PageQAPlugin> plugins, Scheduler scheduler, NcmsEnvironment env) {
        this.plugins = plugins;
        this.scheduler = scheduler;
        this.env = env;


        XMLConfiguration xcfg = env.xcfg();
        //String checkPattern = xcfg.getString("")
    }

    public void checkPages() {
        log.info("Check pages");
    }

    private class CheckPagesTask implements Runnable {

        public void run() {
            checkPages();
        }
    }
}
