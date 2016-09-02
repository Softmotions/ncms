package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.ObjectMapper
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("rs"))
class _TestPageRS(db: String) : BaseRSTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    private var mapper = ObjectMapper()

    @BeforeClass
    override fun setup() {
        super.setup()
        mapper = getEnv()?.injector?.getInstance(ObjectMapper::class.java) ?: ObjectMapper()
    }
}