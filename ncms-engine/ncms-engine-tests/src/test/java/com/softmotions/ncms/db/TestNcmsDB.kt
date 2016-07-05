package com.softmotions.ncms.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Module
import com.softmotions.kotlin.InstancesModule
import com.softmotions.ncms.GuiceBaseTest
import com.softmotions.weboot.liquibase.WBLiquibaseModule
import com.softmotions.weboot.mb.WBMyBatisModule
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test
open class TestNcmsDB : GuiceBaseTest() {

    protected open fun setup(cfgLocation: String, vararg modules: Module) {
        val cfg = loadServicesConfiguration(cfgLocation)
        setupGuice(
                InstancesModule(cfg, ObjectMapper()),
                WBMyBatisModule(cfg),
                WBLiquibaseModule(cfg),
                *modules
        )
    }

    @BeforeClass
    fun setup() {
        setup("com/softmotions/ncms/db/cfg/test-ncms-db-conf.xml")
    }

    @AfterClass
    fun shutdown() {
        super.shutdownGuice()
    }

    @Test
    fun testModel1() {
        println("Test model!!!!");
    }


}