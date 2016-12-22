package com.softmotions.ncms.vedit

import ch.qos.logback.classic.Level
import com.softmotions.ncms.BaseTest
import org.testng.Assert.*
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
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
              <body>
              <div ncms-block="one">
                HelloOne
              </div>

              <b>test</b>

              <div ncms-block='two' class="foo bar">
                HelloTwo
              </div>

              <p ncms-block='three' class="zzz">

              </body>
           </html>
        """

        val res = vf.filter(null, html)
        log.info(res)
        assertTrue(res.contains("#if(ncmsVEBlockExists('one'))$!{ncmsVEBlock('one')}#else"))
        assertTrue(res.contains("#if(ncmsVEBlockExists('two'))$!{ncmsVEBlock('two')}#else"))
        assertTrue(res.contains("HelloOne"))
        assertTrue(res.contains("HelloTwo"))
        assertTrue(res.contains("$!{ncmsVEStyles()}"))
        assertTrue(res.contains("$!{ncmsVEScripts()}"))
        assertTrue(res.contains("class=\"foo bar ncms-block\""))
        assertTrue(res.contains("class=\"ncms-block\""))
        assertTrue(res.contains("<div data-ncms-block=\"$!{ncmsVEBlockId('one')}\" $!{ncmsDocumentVEMeta()} class=\"ncms-block\">"))
        assertTrue(res.contains("data-ncms-block=\"$!{ncmsVEBlockId('one')}\""))
        assertTrue(res.contains("data-ncms-block=\"$!{ncmsVEBlockId('two')}\""))
    }
}