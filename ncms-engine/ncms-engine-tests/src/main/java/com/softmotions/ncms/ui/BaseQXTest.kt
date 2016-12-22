package com.softmotions.ncms.ui

import com.softmotions.commons.JVMResources
import com.softmotions.ncms.UIWebBaseTest
import com.softmotions.web.security.XMLWSUserDatabase
import com.softmotions.web.security.tomcat.WSUserDatabaseRealm
import com.softmotions.weboot.testing.tomcat.TomcatRunner
import org.oneandone.qxwebdriver.By
import org.oneandone.qxwebdriver.QxWebDriver
import org.oneandone.qxwebdriver.ui.Widget
import java.nio.file.Paths


fun Widget.findWidget(qxh: String): Widget = this.findWidget(By.qxh(qxh))

fun Widget.executeInWidget(fspec: String): Any? =
        this.executeJavascript("""
          return (function(){
                ${fspec}
          }).call(qx.ui.core.Widget.getWidgetByElement(arguments[0]))
        """)


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class BaseQXTest(db: String) : UIWebBaseTest(db) {

    val D = "$"

    val qxd: QxWebDriver
        get() = this.driver as QxWebDriver

    open fun setup() {
        val projectBasedir = System.getProperty("project.basedir") ?: throw Exception("Missing required system property: 'project.basedir'")
        val qxRoot = Paths.get(projectBasedir, "..", "ncms-engine-tests-qx/target/qooxdoo/tqx/siteroot").toFile()
        if (!qxRoot.isDirectory) {
            throw Exception("qx.root.dir is not a directory: ${qxRoot.canonicalPath}")
        }
        System.setProperty("qx.root.dir", qxRoot.canonicalPath)
        try {
            setupUITest(
                    "com/softmotions/ncms/ui/cfg/test-ncms-ui-conf.xml",
                    driverType = "qx")
        } catch (tr: Throwable) {
            shutdown()
            throw tr
        }
    }

    fun qxpn(qxclass: String, text: String? = null): String {
        return "div[@qxclass='${qxclass}' ${if (text != null) " and text()='${text}'" else ""}]"

    }

    override fun configureTomcatRunner(b: TomcatRunner.Builder) {
        super.configureTomcatRunner(b)
        val wsdb = XMLWSUserDatabase("WSUserDatabase", "com/softmotions/ncms/ui/cfg/users.xml", false, "sha256")
        JVMResources.set(wsdb.databaseName, wsdb)
        b.withRealm(WSUserDatabaseRealm(wsdb))
    }

    override fun R(resource: String): String {
        return super.R("admin:ncms1", resource)
    }

    protected fun findWidget(h: String): Widget {
        return qxd.findWidget(By.qxh(h))
    }

    protected fun findWidget(by: org.openqa.selenium.By): Widget {
        return qxd.findWidget(by)
    }

    protected fun waitForWidget(h: String, timeout: Long = 5): Widget {
        return qxd.waitForWidget(By.qxh(h), timeout)
    }
}