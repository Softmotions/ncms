package com.softmotions.ncms

import om.softmotions.weboot.testing.tomcat.TomcatRunner
import java.nio.file.Paths

/**
 * Base class for web tests
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class WebBaseTest {

    private var runner: TomcatRunner? = null

    private var projectBasedir: String? = null

    @Throws(Exception::class)
    fun setUp() {
        projectBasedir = System.getProperty("project.basedir")
        if (projectBasedir == null) {
            throw Exception("Missing required system property: 'project.basedir'")
        }
        System.getProperty("WEBOOT_CFG_LOCATION") ?:
                throw Exception("Missing required system property: 'WEBOOT_CFG_LOCATION'")
    }

    @Throws(Exception::class)
    fun setUpWeb() {
        setUp()
        val b = TomcatRunner.createBuilder()
        configureJettyRunner(b)
        runner = b.build()
    }

    protected fun configureJettyRunner(b: TomcatRunner.Builder) {
        b.withPort(8282)
                .withContextPath("/")
                .withResourcesBase(getBaseWebappDir())
    }

    protected fun getBaseWebappDir(): String {
        return Paths.get(projectBasedir, "src/main/webapp").toString()
    }


    protected fun R(resource: String): String {
        return R(null, resource)
    }

    protected fun R(up: String?, resource: String): String {
        val sb = StringBuilder(64)
        sb.append("http://")
        if (up != null) {
            sb.append(up).append('@')
        }
        sb.append("localhost:")
        sb.append(runner!!.usedBuilder().getPort())
        if (resource[0] != '/') {
            sb.append('/')
        }
        sb.append(resource)
        return sb.toString()
    }

}