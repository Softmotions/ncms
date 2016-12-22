package com.softmotions.ncms.db

import com.softmotions.ncms.DbTestsFactory

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class TestDBs : DbTestsFactory() {

    override fun createTests(db: String): Array<Any> {
        return arrayOf(
                _TestAsmDAO(db),
                _TestAsmRSDB(db),
                _TestDBSpecificQueries(db)
                )
    }

}