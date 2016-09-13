package com.softmotions.ncms.ui

import com.softmotions.commons.JVMResources
import com.softmotions.ncms.UIWebBaseTest
import com.softmotions.web.security.XMLWSUserDatabase
import com.softmotions.web.security.tomcat.WSUserDatabaseRealm
import com.softmotions.weboot.testing.tomcat.TomcatRunner

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class BaseQXTest(db: String) : UIWebBaseTest(db) {

    open fun setup() {
        try {
            setupUITest(
                    "com/softmotions/ncms/ui/cfg/test-ncms-ui-conf.xml",
                    driverType = "qx")
        } catch (tr: Throwable) {
            shutdown()
            throw tr
        }
    }

    override fun configureTomcatRunner(b: TomcatRunner.Builder) {
        super.configureTomcatRunner(b)
        val wsdb = XMLWSUserDatabase("WSUserDatabase", "com/softmotions/ncms/ui/cfg/users.xml", false)
        JVMResources.set(wsdb.databaseName, wsdb)
        b.withRealm(WSUserDatabaseRealm(wsdb))
    }
}