package com.softmotions.ncms

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.Stage
import com.softmotions.commons.ServicesConfiguration
import com.softmotions.commons.lifecycle.LifeCycleModule
import com.softmotions.commons.lifecycle.LifeCycleService
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class GuiceBaseTest {

    @JvmField
    protected val log = LoggerFactory.getLogger(javaClass)

    protected var injector: Injector? = null

    protected fun setupLogging(level: Level = Level.ERROR) {
        if (LoggerFactory.getILoggerFactory() is LoggerContext) {
            val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
            ctx.getLogger("ROOT").level = level
        }
    }

    protected fun loadServicesConfiguration(cfgLocation: String): ServicesConfiguration {
        return ServicesConfiguration(cfgLocation);
    }

    protected open fun setupGuice(vararg modules: Module) {
        val mlist = ArrayList<Module>()
        mlist += LifeCycleModule()
        mlist += modules
        injector = Guice.createInjector(Stage.PRODUCTION, mlist)
        injector?.getInstance(LifeCycleService::class.java)?.start()
    }

    protected open fun shutdownGuice() {
        try {
            injector?.getInstance(LifeCycleService::class.java)?.stop()
        } catch (e: Throwable) {
            log.error("", e)
        }
    }

    protected fun <T : Any> getInstance(kclass: KClass<T>): T {
        return injector?.getInstance(kclass.java)!!
    }
}