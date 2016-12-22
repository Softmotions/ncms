package com.softmotions.ncms.rs

import com.softmotions.ncms.DbTestsFactory


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class TestRS : DbTestsFactory() {

    override fun createTests(db: String): Array<out Any> {
        return arrayOf(
                _TestAsmRS(db),
                _TestPageRS(db),
                _TestMediaRS(db),
                _TestMttRulesRS(db))
    }
}