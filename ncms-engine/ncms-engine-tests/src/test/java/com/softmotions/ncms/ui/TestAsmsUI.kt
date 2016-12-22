package com.softmotions.ncms.ui

import com.softmotions.ncms.DbTestsFactory
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class TestAsmsUI : DbTestsFactory() {

    @Test
    override fun createTests(db: String): Array<out Any> {
        return arrayOf(_TestSimpleSiteUI(db))
    }
}