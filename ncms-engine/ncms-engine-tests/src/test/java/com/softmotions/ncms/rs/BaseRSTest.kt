package com.softmotions.ncms.rs

import com.softmotions.commons.JVMResources
import com.softmotions.ncms.WebBaseTest
import com.softmotions.web.security.XMLWSUserDatabase
import com.softmotions.web.security.tomcat.WSUserDatabaseRealm
import com.softmotions.weboot.testing.tomcat.TomcatRunner
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
open class BaseRSTest : WebBaseTest() {

    @BeforeClass
    open fun setup() {
        System.getProperty("liquibase.dropAll") ?: System.setProperty("liquibase.dropAll", "false")
        System.getProperty("ncmstest.ds") ?: System.setProperty("ncmstest.ds", "${System.getProperty("user.home")}/.ncms-test.ds")
        System.setProperty("WEBOOT_CFG_LOCATION", "com/softmotions/ncms/rs/cfg/test-ncms-rs-conf.xml")
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
    open fun shutdown() {
        shutdownWeb()
    }

}