package com.softmotions.ncms.vedit

import ch.qos.logback.classic.Level
import com.softmotions.ncms.BaseTest
import org.testng.Assert.*
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("unit"))
class TestVEditFilter : BaseTest() {

    init {
        setupLogging(Level.INFO)
    }

    @Test
    fun testHtmlProcessing() {

        val vf = HttlVisualEditorFilter()
        val html = """
           <html>
              <div ncms-block="one">
                HelloOne
              </div>

              <b>test</b>

              <div ncms-block='two'>
                HelloTwo
              </div>
           </html>
        """

        val res = vf.filter(null, html)
        log.info(res)
        assertTrue(res.contains("#if(ncmsVEBlockExists('one')) $!{ncmsVEBlock('one')} #else"))
        assertTrue(res.contains("#if(ncmsVEBlockExists('two')) $!{ncmsVEBlock('two')} #else"))
        assertTrue(res.contains("HelloOne"))
        assertTrue(res.contains("HelloTwo"))
        assertTrue(res.contains("$!{ncmsVEStyles()}"))
        assertTrue(res.contains("$!{ncmsVEScripts()}"))
    }
}