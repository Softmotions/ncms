package com.softmotions.ncms

import ch.qos.logback.classic.Level
import com.github.kevinsawicki.http.HttpRequest
import com.google.inject.Injector
import com.softmotions.weboot.testing.tomcat.TomcatRunner
import java.nio.file.Paths

/**
 * Base class for web tests
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class WebBaseTest(db: String) : DbBaseTest(db) {

    protected var runner: TomcatRunner? = null

    protected var projectBasedir: String? = null

    override var injector: Injector? = null
        get() = getEnv()!!.injector

    open fun setupWeb() {
        setupLogging(Level.INFO)
        log.info("setupWeb")
        setupDb()
        projectBasedir = System.getProperty("project.basedir") ?: throw Exception("Missing required system property: 'project.basedir'")
        System.getProperty("WEBOOT_CFG_LOCATION") ?: throw Exception("Missing required system property: 'WEBOOT_CFG_LOCATION'")
        val b = TomcatRunner.createBuilder()
        configureTomcatRunner(b)
        runner = b.build()
    }

    open fun shutdownWeb() {
        log.info("shutdownWeb")
        try {
            runner?.shutdown()
        } catch(e: Throwable) {
            log.error("", e)
        }
    }


    override fun shutdown() {
        shutdownWeb()
        shutdownDb()
    }

    open protected fun configureTomcatRunner(b: TomcatRunner.Builder) {
        b.withPort(8282)
                .withContextPath("")
                .withResourcesBase(getBaseWebappDir())
    }

    open protected fun getBaseWebappDir(): String {
        return Paths.get(projectBasedir, "src/test/webapp").toString()
    }

    open protected fun getEnv(): NcmsBoot? {
        return runner?.getContextEventListener(NcmsBoot::class.java)
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
    open protected fun HEAD(resource: String): HttpRequest = auth(HttpRequest.head(R(resource)))
    open protected fun PUT(resource: String): HttpRequest = auth(HttpRequest.put(R(resource)))
    open protected fun POST(resource: String): HttpRequest = auth(HttpRequest.post(R(resource)))
    open protected fun DELETE(resource: String): HttpRequest = auth(HttpRequest.delete(R(resource)))
}