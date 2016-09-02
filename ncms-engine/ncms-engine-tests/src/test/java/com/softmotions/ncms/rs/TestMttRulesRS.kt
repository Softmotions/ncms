package com.softmotions.ncms.rs

import com.softmotions.ncms.DbTestsFactory


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
class TestMttRulesRS : DbTestsFactory() {

    override fun createTest(db: String): Array<out Any> {
        return arrayOf(_TestMttRulesRS(db), _TestWorkspaceRS(db))
    }
}