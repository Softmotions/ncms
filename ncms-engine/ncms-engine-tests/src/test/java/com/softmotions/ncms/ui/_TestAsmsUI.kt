package com.softmotions.ncms.ui

import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("ui"))
class _TestAsmsUI(db: String) : BaseQXTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    @BeforeClass
    override fun setup() = super.setup()

    @AfterClass
    override fun shutdown() = super.shutdown()

    @Test
    fun testUI1() {
        log.info("!!!!!!!!!!!!!!!!!!! TEST UI !!!!!!!!!!!!!!!!!!!!")
        //pauseDriver(600)
    }
}