package com.softmotions.ncms.rs

import org.testng.Assert.assertEquals
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("rs"))
class TestWorkspaceRS : BaseRSTest() {

    @BeforeClass
    override fun setup() {
        super.setup()
    }

    @AfterClass
    override fun shutdown() {
        super.shutdown()
    }

    @Test
    fun testWorkspaceRS() {
        with(GET("/rs/adm/ws/state")) {
            assertEquals(200, code())
            //todo
            log.info("Body={}", body())
        }

        val env = getEnv()
        log.info("env={}", env)
    }
}