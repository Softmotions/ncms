package com.softmotions.ncms

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.ObjectMapper
import com.softmotions.kotlin.InstancesModule
import com.softmotions.weboot.liquibase.WBLiquibaseModule
import com.softmotions.weboot.mb.WBMyBatisModule

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class DbBaseTest(db: String) : GuiceBaseTest() {

    val dbTestRunner: DatabaseTestRunner

    companion object {
        val DEFAULT_DB = "db2"
    }

    init {

        when (db) {
            "db2" -> {
                dbTestRunner = Db2TestRunner()
            }
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
        dbTestRunner.shutdownDb()
    }


    open fun setup(cfgLocation: String) {
        setupLogging(Level.INFO)
        setupDb()
        try {
            val cfg = loadServicesConfiguration(cfgLocation)
            setupGuice(
                    InstancesModule(cfg, ObjectMapper()),
                    WBMyBatisModule(cfg),
                    WBLiquibaseModule(cfg)
            )
        } catch (tr: Throwable) {
            shutdownDb()
            throw tr
        }
    }

    open fun shutdown() {
        try {
            shutdownGuice()
        } finally {
            shutdownDb()
        }
    }
}