package com.softmotions.ncms.vedit

import httl.spi.filters.AbstractFilter
import net.htmlparser.jericho.OutputDocument
import net.htmlparser.jericho.Source
import org.slf4j.LoggerFactory

/**
 * Visual editor HTTL filter.
 *
 * Filter concept is based on [httl.spi.filters.AttributeSyntaxFilter]
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
class HttlVisualEditorFilter : AbstractFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(key: String?, value: String?): String {
        val source = Source(value)
        val document = OutputDocument(source)
        process(source, document)
        if (log.isDebugEnabled) {
            log.debug("Source: {} \n\nTransformed: {}", value, document)
        }
        return document.toString();
    }

    private fun process(source: Source, document: OutputDocument) {
        val allElements = source.allElements
        allElements.find {
            it.getAttributeValue("ncms-block") != null
        } ?: return

        for (el in allElements) {
            if (el.name == "html") {
                document.insert(el.endTag.begin, "\n$!{ncmsVEStyles()}")
                document.insert(el.endTag.begin, "\n$!{ncmsVEScripts()}\n")
                continue
            }
            val attr = el.attributes.get("ncms-block") ?: continue
            document.remove(attr.begin - 1, attr.end)
            document.insert(el.begin, "#if(ncmsVEBlockExists('${attr.value}')) $!{ncmsVEBlock('${attr.value}')} #else ")
            document.insert(el.end, " #end")
        }
    }
}