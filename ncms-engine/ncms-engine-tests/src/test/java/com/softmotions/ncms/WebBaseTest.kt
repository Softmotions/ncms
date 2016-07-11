package com.softmotions.ncms

import com.github.kevinsawicki.http.HttpRequest
import com.softmotions.weboot.testing.tomcat.TomcatRunner
import org.slf4j.LoggerFactory
import java.nio.file.Paths

/**
 * Base class for web tests
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class WebBaseTest {

    @JvmField
    protected val log = LoggerFactory.getLogger(javaClass)

    protected var runner: TomcatRunner? = null

    protected var projectBasedir: String? = null

    @Throws(Exception::class)
    open fun setUp() {
        projectBasedir = System.getProperty("project.basedir") ?: throw Exception("Missing required system property: 'project.basedir'")
        System.getProperty("WEBOOT_CFG_LOCATION") ?: throw Exception("Missing required system property: 'WEBOOT_CFG_LOCATION'")
    }

    @Throws(Exception::class)
    open fun setupWeb() {
        setUp()
        val b = TomcatRunner.createBuilder()
        configureTomcatRunner(b)
        runner = b.build()
    }

    @Throws(Exception::class)
    open fun shutdownWeb() {
        runner?.shutdown()
    }

    open protected fun configureTomcatRunner(b: TomcatRunner.Builder) {
        b.withPort(8282)
                .withContextPath("")
                .withResourcesBase(getBaseWebappDir())
    }

    open protected fun getBaseWebappDir(): String {
        return Paths.get(projectBasedir, "src/main/webapp").toString()
    }

    open protected fun getEnv(): NcmsServletListener? {
        return runner?.getContextEventListener(NcmsServletListener::class.java)
    }


    open protected fun auth(r: HttpRequest): HttpRequest {
        return auth("admin", "ncms1", r)
    }

    open protected fun auth(login: String, passwd: String, r: HttpRequest): HttpRequest {
        r.basic(login, passwd)
        return r
    }

    open protected fun R(resource: String): String {
        return R(null, resource)
    }

    open protected fun R(up: String?, resource: String): String {
        val sb = StringBuilder(64)
        sb.append("http://")
        if (up != null) {
            sb.append(up).append('@')
        }
        sb.append("localhost:")
        sb.append(runner!!.usedBuilder().port)
        if (resource[0] != '/') {
            sb.append('/')
        }
        sb.append(resource)
        return sb.toString()
    }

    open protected fun GET(resource: String): HttpRequest = auth(HttpRequest.get(R(resource)))
    open protected fun PUT(resource: String): HttpRequest = auth(HttpRequest.put(R(resource)))
    open protected fun POST(resource: String): HttpRequest = auth(HttpRequest.post(R(resource)))
    open protected fun DELETE(resource: String): HttpRequest = auth(HttpRequest.delete(R(resource)))
}