package com.softmotions.ncms

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.Stage
import com.softmotions.commons.ServicesConfiguration
import com.softmotions.commons.lifecycle.LifeCycleModule
import com.softmotions.commons.lifecycle.LifeCycleService
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class GuiceBaseTest : BaseTest() {

    open protected val injector: Injector?
        get() = _injector

    private var _injector: Injector? = null

    protected fun loadServicesConfiguration(cfgLocation: String): ServicesConfiguration {
        return ServicesConfiguration(cfgLocation);
    }

    protected open fun setupGuice(vararg modules: Module) {
        val mlist = ArrayList<Module>()
        mlist += LifeCycleModule()
        mlist += modules
        _injector = Guice.createInjector(Stage.PRODUCTION, mlist)
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

    protected fun <T> getInstance(clazz: Class<T>): T {
        return injector!!.getInstance(clazz)!!
    }
}