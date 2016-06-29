package com.softmotions.ncms.qa;

import java.util.Set;
import javax.ws.rs.Path;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.lifecycle.Dispose;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.HttpServletRequestAdapter;
import com.softmotions.weboot.mb.MBDAOSupport;

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

    @Override
    public void run() {
        while (true) {
            long sleep = env.xcfg().getLong("getLongbatch-pause-seconds", 60) * 1000;
            processBatch();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
                break;
            }
        }
        log.info("{} finished", Thread.currentThread().getName());
    }


    void processBatch() {
        //PageQAContext ctx = createPageQAContext();

    }


    private PageQAContext createPageQAContext() {
        return new PageQAContext();
    }


    private static class PageQARequest extends HttpServletRequestAdapter {

        @Override
        public String getMethod() {
            return "GET";
        }

        @Override
        public String getRemoteUser() {
            return "system";
        }


    }
}
