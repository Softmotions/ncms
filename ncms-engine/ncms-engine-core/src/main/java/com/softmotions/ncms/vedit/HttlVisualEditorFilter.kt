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
            if (el.name == "body") {
                document.insert(el.endTag.begin, "\n$!{ncmsVEStyles()}\n$!{ncmsVEScripts()}")
                continue
            }
            val blockAttr = el.attributes.get("ncms-block") ?: continue
            val attr2 = el.attributes.get("class")
            if (attr2 == null) {
                document.insert(el.startTag.end - 1, " class=\"ncms-block\"")
            } else {
                document.remove(attr2.valueSegment)
                document.insert(attr2.valueSegment.begin, attr2.value + " ncms-block")
            }
            document.replace(blockAttr, "data-ncms-block=\"${blockAttr.value}\"")
            document.insert(el.begin, "#if(ncmsVEBlockExists('${blockAttr.value}')) $!{ncmsVEBlock('${blockAttr.value}')} #else ")
            document.insert(el.end, " #end")
        }
    }
}