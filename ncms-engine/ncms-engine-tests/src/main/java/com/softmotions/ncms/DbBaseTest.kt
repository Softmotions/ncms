package com.softmotions.ncms

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.ObjectMapper
import com.softmotions.kotlin.InstancesModule
import com.softmotions.ncms.events.EventsModule
import com.softmotions.weboot.liquibase.WBLiquibaseModule
import com.softmotions.weboot.mb.WBMyBatisModule

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class DbBaseTest(db: String) : GuiceBaseTest() {

    val dbTestRunner: DatabaseTestRunner


    companion object {
        @JvmField
        val DEFAULT_DB = ""
    }


    init {

        when (db) {
            "db2" -> {
                dbTestRunner = DB2TestRunner()
            }
            "",
            "postgresql",
            "postgres" -> {
                dbTestRunner = PostgresTestRunner()
            }
            else -> throw RuntimeException("Invalid database id: ${db}")
        }
    }

    open fun setupDb(props: Map<String, Any> = emptyMap<String, Any>()) {
        val sysprops = System.getProperties()
        val pnames = sysprops.propertyNames()
        while (pnames.hasMoreElements()) {
            val pname = pnames.nextElement() as String
            if (pname.startsWith("JDBC.")) {
                sysprops.remove(pname)
            }
        }
        // todo review
        System.setProperty("liquibase.dropAll", "true");
        log.info("\n\n\nDATABASE RUNNER: ${dbTestRunner.javaClass.name}")
        dbTestRunner.setupDb(props)

    }

    open fun shutdownDb() {
        log.info("shutdownDb")
        try {
            dbTestRunner.shutdownDb()
        } catch (e: Throwable) {
            log.error("", e)
        }
    }

    open fun setup(cfgLocation: String) {
        setupLogging(Level.INFO)
        setupDb()
        try {
            val cfg = loadServicesConfiguration(cfgLocation)
            setupGuice(
                    InstancesModule(cfg, ObjectMapper()),
                    WBMyBatisModule(cfg),
                    WBLiquibaseModule(cfg),
                    EventsModule()
            )
        } catch (tr: Throwable) {
            shutdownDb()
            throw tr
        }
    }

    protected open fun shutdown() {
        shutdownGuice()
        shutdownDb()
    }
}