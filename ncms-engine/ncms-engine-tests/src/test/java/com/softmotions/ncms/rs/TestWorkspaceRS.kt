package com.softmotions.ncms.rs

import com.github.kevinsawicki.http.HttpRequest
import com.softmotions.commons.JVMResources
import com.softmotions.ncms.NcmsServletListener
import com.softmotions.ncms.WebBaseTest
import com.softmotions.web.security.XMLWSUserDatabase
import com.softmotions.web.security.tomcat.WSUserDatabaseRealm
import com.softmotions.weboot.testing.tomcat.TomcatRunner
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.Assert.*

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
class TestWorkspaceRS : WebBaseTest() {

    @BeforeClass
    fun setup() {
        System.setProperty("liquibase.dropAll", "false")
        if (System.getProperty("ncmstest.ds") == null) {
            System.setProperty("ncmstest.ds",
                    System.getProperty("user.home") + "/.ncms-test.ds")
        }
        System.setProperty("WEBOOT_CFG_LOCATION",
                "com/softmotions/ncms/rs/cfg/test-ncms-rs-conf.xml")
        setupWeb()
        runner!!.start()
    }

    override fun configureTomcatRunner(b: TomcatRunner.Builder) {
        super.configureTomcatRunner(b)
        val wsdb = XMLWSUserDatabase("WSUserDatabase", "com/softmotions/ncms/rs/cfg/users.xml", false)
        JVMResources.set(wsdb.databaseName, wsdb);
        b.withRealm(WSUserDatabaseRealm(wsdb))
    }

    @AfterClass
    fun shutdown() {
        shutdownWeb()
    }

    @Test
    fun testWorkspaceRS() {
        var req = auth(HttpRequest.get(R("/rs/adm/ws/state")))
        assertEquals(req.code(), 200)
        //todo
        log.info("Body={}", req.body())

        val env = runner!!.getContextEventListener(NcmsServletListener::class.java)
        log.info("env=" + env)
    }

}