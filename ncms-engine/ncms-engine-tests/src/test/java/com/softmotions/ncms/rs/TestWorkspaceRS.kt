package com.softmotions.ncms.rs

import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("rs"))
class TestWorkspaceRS : BaseRSTest() {

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