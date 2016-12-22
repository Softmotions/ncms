package com.softmotions.ncms.js;

import java.nio.file.Paths;
import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.softmotions.commons.JVMResources;
import com.softmotions.ncms.WebBaseTest;
import com.softmotions.web.security.XMLWSUserDatabase;
import com.softmotions.web.security.tomcat.WSUserDatabaseRealm;
import com.softmotions.weboot.testing.tomcat.TomcatRunner;

/**
 * Basic test for JsServiceRS
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Test(groups = "rs")
public class _TestJsServiceRS extends WebBaseTest {

    JsServiceRS jsServiceRS;

    @BeforeClass
    public void setup() {
        System.setProperty("WEBOOT_CFG_LOCATION", "com/softmotions/ncms/js/cfg/test-ncms-js-conf.xml");
        try {
            setupWeb();
            log.info("Starting runner");
            getRunner().start();
            log.warn("{}", getRunner());
            jsServiceRS = getInjector().getInstance(JsServiceRS.class);
        } catch (Throwable tr) {
            log.error("", tr);
            shutdownDb();
            throw new RuntimeException(tr);
        }
    }

    @AfterClass
    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    protected void configureTomcatRunner(TomcatRunner.@NotNull Builder b) {
        super.configureTomcatRunner(b);
        XMLWSUserDatabase wsdb =
                new XMLWSUserDatabase("WSUserDatabase", "com/softmotions/ncms/js/cfg/users.xml", false);
        JVMResources.set(wsdb.getDatabaseName(), wsdb);
        b.withRealm(new WSUserDatabaseRealm(wsdb));
    }

    @Override
    @Nonnull
    protected String getBaseWebappDir() {
        return Paths.get(getProjectBasedir(), "src/test/webapp").toString();
    }

    public _TestJsServiceRS(@Nonnull String db) {
        super(db);
    }

    public _TestJsServiceRS() {
        super(DEFAULT_DB);
    }

    ///////////////////////////////////////////////////////////////////////////
    //                         Test methods                                  //
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void testBasicMethods() throws Exception {
        // todo
    }
}
