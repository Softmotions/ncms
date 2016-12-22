package com.softmotions.ncms.rs

import org.testng.Assert.*
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Test(groups = arrayOf("rs"))
class _TestWorkspaceRS(db: String) : BaseRSTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

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
            assertEquals(code(), 200)
            //todo
            log.info("Body={}", body())
        }

        val env = getEnv()
        log.info("env={}", env)
    }
}
