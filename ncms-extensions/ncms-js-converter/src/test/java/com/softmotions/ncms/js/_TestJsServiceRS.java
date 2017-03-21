package com.softmotions.ncms.js;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Key;
import com.softmotions.commons.JVMResources;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.ncms.NcmsModuleDescriptor;
import com.softmotions.ncms.WebBaseTest;
import com.softmotions.ncms.atm.ServerMessageEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.MediaRepository;
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

    MediaRepository mediaRepository;

    NcmsEventBus ebus;

    DataSource ds;

    ServerMessageEvent lastServerMessageEvent;

    @BeforeClass
    public void setup() {
        System.setProperty("WEBOOT_CFG_LOCATION", "com/softmotions/ncms/js/cfg/test-ncms-js-conf.xml");
        try {
            setupWeb();
            log.info("Starting runner");
            getRunner().start();
            log.warn("{}", getRunner());
            jsServiceRS = getInstance(JsServiceRS.class);
            mediaRepository = getInstance(MediaRepository.class);
            ebus = getInstance(NcmsEventBus.class);
        } catch (Throwable tr) {
            log.error("", tr);
            shutdownDb();
            throw new RuntimeException(tr);
        }

        // SqlSession
        ds = getInstance(DataSource.class);

        // Activate testing mode
        jsServiceRS.testingMode = true;
        ebus.register(this);
    }

    @AfterClass
    @Override
    public void shutdown() {
        ebus.unregister(this);
        super.shutdown();
    }

    @Override
    protected void configureTomcatRunner(TomcatRunner.Builder b) {
        super.configureTomcatRunner(b);
        XMLWSUserDatabase wsdb =
                new XMLWSUserDatabase("WSUserDatabase", "com/softmotions/ncms/js/cfg/users.xml", false);
        JVMResources.set(wsdb.getDatabaseName(), wsdb);
        b.withRealm(new WSUserDatabaseRealm(wsdb));
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
    public void testIfModuleMetaPresented() throws Exception {
        Set<NcmsModuleDescriptor> descriptors =
                getInjector().getInstance(new Key<Set<NcmsModuleDescriptor>>() {
                });
        Optional<NcmsModuleDescriptor> val =
                descriptors.stream()
                           .filter(md -> md.getModuleClass() == NcmsJsModule.class)
                           .findFirst();
        assertTrue(val.isPresent());
    }

    /**
     * Test js syntax checking
     */
    @Test
    public void testCheckSyntax() throws Exception {

        // invalid js
        lastServerMessageEvent = null;
        String js = "function fn1(foo){var bar = 'bar';\nconsole.log('foo=' + bar;\n}";
        mediaRepository.importFile(IOUtils.toInputStream(js, "utf8"), "/testdata/js1.js", false, null);
        assertTrue(waitForPredicate(() -> lastServerMessageEvent != null));
        String msg = lastServerMessageEvent.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("/testdata/js1.js:2: ERROR - Parse error. ',' expected"));

        // valid js
        lastServerMessageEvent = null;
        js = "function fn1(foo){var bar = 'bar';\nconsole.log('foo=' + bar);\n}";
        mediaRepository.importFile(IOUtils.toInputStream(js, "utf8"), "/testdata/js1.js", false, null);
        // we need to wait for completion of async tasks
        assertFalse(waitForPredicate(() -> lastServerMessageEvent != null, 1000L));
    }

    /**
     * Test basic js compilation
     */
    @Test
    public void testScriptCompilationAndJsResources() throws Exception {

        lastServerMessageEvent = null;
        String js1 = "function fn1(foo){var bar = 'bfc63bdfb749';\nconsole.log('beb6=' + bar);\n}";
        mediaRepository.importFile(IOUtils.toInputStream(js1, "utf8"), "/testdata/js1.js", false, null);
        String js2 = "function fn2(foo){var bar = '808535b1ee2e';\nconsole.log('899e=' + bar);\n}";
        mediaRepository.importFile(IOUtils.toInputStream(js2, "utf8"), "/testdata/js2.js", false, null);
        String[] scripts = new String[]{"/testdata/js1.js", "/testdata/js2.js"};
        String scriptRef = jsServiceRS.createScriptRef(scripts, Collections.emptyMap());
        assertNotNull(scriptRef);
        // Eg: /rs/x/js/script/be1914e01d9917362786ab1ee1996448.js
        HttpRequest req = GET(scriptRef);
        assertEquals(req.code(), 200);
        String compiledScript = req.body();
        assertEquals(compiledScript, "function fn1(a){console.log(\"beb6\\x3dbfc63bdfb749\")};function fn2(a){console.log(\"899e\\x3d808535b1ee2e\")};");

        String fp = jsServiceRS.computeFingerprint(scripts, Collections.emptyMap());
        try (Connection conn = ds.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM x_js_spec WHERE fingerprint = ?")) {
                ps.setString(1, fp);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    String fp1 = rs.getString("fingerprint");
                    assertEquals(fp, fp1);
                    //noinspection MismatchedQueryAndUpdateOfCollection
                    KVOptions spec = new KVOptions(rs.getString("spec"));
                    assertEquals(spec.getString("in"), "es5");
                    assertEquals(spec.getString("out"), "es5");
                    assertEquals(spec.getString("level"), "simple");
                    assertNotNull(spec.get("scripts"));
                }
            }
        }
    }

    // todo test cleanup, etc..

    ///////////////////////////////////////////////////////////////////////////
    //                        Event handlers                                 //
    ///////////////////////////////////////////////////////////////////////////

    @Test(enabled = false)
    @Subscribe
    public void onServerMessageEvent(ServerMessageEvent ev) {
        lastServerMessageEvent = ev;
    }
}
