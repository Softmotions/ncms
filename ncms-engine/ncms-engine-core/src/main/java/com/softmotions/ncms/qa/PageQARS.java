package com.softmotions.ncms.qa;

import com.softmotions.weboot.scheduler.Scheduled;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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

    private final Set<PageQAModule> modules;

    @Inject
    public PageQARS(Set<PageQAModule> modules) {
        this.modules = modules;
    }


    @Scheduled("* * * * *")
    public void checkPages() {
       log.info("Check pages");
    }
}
