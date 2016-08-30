package com.softmotions.ncms.db

import com.softmotions.ncms.DbBaseTest
import com.softmotions.ncms.asm.AsmRS
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.ibatis.session.SqlSession
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Testing of sql queries for AsmRS
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
class _TestAsmRSDB(db: String) : DbBaseTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    val ds: MBDAOSupport by lazy {
        MBDAOSupport(AsmRS::class.java, getInstance(SqlSession::class))
    }

    @BeforeClass
    fun setup() {
        super.setup("com/softmotions/ncms/db/cfg/test-ncms-db-conf.xml")
    }

    @AfterClass
    override fun shutdown() {
        super.shutdown()
    }

    // todo write AsmRS.xml sql test here

    @Test
    fun testSelect() {
        ds.select<Map<String,Any>>("select")
    }


}