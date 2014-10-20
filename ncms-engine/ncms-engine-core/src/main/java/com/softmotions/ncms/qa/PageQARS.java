package com.softmotions.ncms.qa;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.HttpServletRequestAdapter;
import com.softmotions.weboot.lifecycle.Dispose;
import com.softmotions.weboot.lifecycle.Start;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.util.Set;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/pages/qa")
@Singleton
public class PageQARS extends MBDAOSupport implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PageQARS.class);

    private final Set<PageQAPlugin> plugins;

    private final NcmsEnvironment env;

    private Thread checkThread;


    @Inject
    public PageQARS(Set<PageQAPlugin> plugins, NcmsEnvironment env, SqlSession sess) {
        super(PageQARS.class, sess);
        this.plugins = plugins;
        this.env = env;
    }

    @Start(order = Integer.MAX_VALUE)
    public void start() {
        if (checkThread != null && checkThread.isAlive()) {
            checkThread.interrupt();
            checkThread = null;
        }
        checkThread = new Thread(this, PageQARS.class.getSimpleName());
        checkThread.start();
    }


    @Dispose(order = Integer.MAX_VALUE)
    public void stop() {
        if (checkThread != null && checkThread.isAlive()) {
            checkThread.interrupt();
        }
    }

    public void run() {
        while (true) {
            long sleep = env.xcfg().getLong("getLongbatch-pause-seconds", 60) * 1000;
            processBatch();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                break;
            }
        }
        log.info(Thread.currentThread().getName() + " finished");
    }



    void processBatch() {
        PageQAContext ctx = createPageQAContext();

    }


    private PageQAContext createPageQAContext() {
        PageQAContext ctx = new PageQAContext();
        return ctx;
    }


    private class PageQARequest extends HttpServletRequestAdapter {

        public String getMethod() {
            return "GET";
        }

        public String getRemoteUser() {
            return "system";
        }


    }
}
