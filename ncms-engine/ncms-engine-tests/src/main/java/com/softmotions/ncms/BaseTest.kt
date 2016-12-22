package com.softmotions.ncms

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class BaseTest {

    @JvmField
    protected val log = LoggerFactory.getLogger(javaClass)

    protected fun setupLogging(level: Level = Level.ERROR) {
        if (LoggerFactory.getILoggerFactory() is LoggerContext) {
            val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
            ctx.getLogger("ROOT").level = level
        }
    }

    protected fun waitForPredicate(testfn: Callable<Boolean>): Boolean {
        return waitForPredicate(testfn, 5000L);
    }

    protected fun waitForPredicate(testfn: Callable<Boolean>, ms: Long): Boolean {
        val s = 50L
        var t = 0L
        do {
            if (testfn.call()) {
                return true
            }
            try {
                Thread.sleep(s)
            } catch(ignored: Exception) {
            }
            t += s
        } while (t < ms)
        return false
    }

}