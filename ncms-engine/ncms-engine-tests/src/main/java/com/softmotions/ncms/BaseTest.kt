package com.softmotions.ncms

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory

/**
 * @author Adamansky Anton (adamansky@gmail.com)
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
}