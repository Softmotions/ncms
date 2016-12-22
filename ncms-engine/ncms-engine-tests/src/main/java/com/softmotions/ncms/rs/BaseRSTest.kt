package com.softmotions.ncms.rs

import com.softmotions.commons.JVMResources
import com.softmotions.ncms.WebBaseTest
import com.softmotions.web.security.XMLWSUserDatabase
import com.softmotions.web.security.tomcat.WSUserDatabaseRealm
import com.softmotions.weboot.testing.tomcat.TomcatRunner

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
open class BaseRSTest(db: String) : WebBaseTest(db) {

    open fun setup() {
        System.setProperty("WEBOOT_CFG_LOCATION", "com/softmotions/ncms/rs/cfg/test-ncms-rs-conf.xml")
        try {
            setupWeb()
            log.info("Starting runner")
            runner!!.start()
            log.warn("{}", runner)
        } catch (tr: Throwable) {
            log.error("", tr)
            shutdownDb()
            throw tr
        }
    }

    override fun configureTomcatRunner(b: TomcatRunner.Builder) {
        super.configureTomcatRunner(b)
        val wsdb = XMLWSUserDatabase("WSUserDatabase", "com/softmotions/ncms/rs/cfg/users.xml", false, "sha256")
        JVMResources.set(wsdb.databaseName, wsdb)
        b.withRealm(WSUserDatabaseRealm(wsdb))
    }
}