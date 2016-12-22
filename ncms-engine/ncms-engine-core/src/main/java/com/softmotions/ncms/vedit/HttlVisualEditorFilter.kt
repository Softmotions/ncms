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
 * @author Adamansky Anton (adamansky@softmotions.com)
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
        var metaInjected = false;
        val allElements = source.allElements
        allElements.find {
            it.getAttributeValue("ncms-block") != null
        } ?: return

        for (el in allElements) {
            val blockAttr = el.attributes?.get("ncms-block") ?: continue
            if (!metaInjected) {
                metaInjected = true
                document.insert(el.begin, "\n$!{ncmsVEStyles()}\n$!{ncmsVEScripts()}\n")
                document.insert(el.attributes.end, " $!{ncmsDocumentVEMeta()}");
            }
            val attr2 = el.attributes.get("class")
            if (attr2 == null) {
                document.insert(el.startTag.end - 1, " class=\"ncms-block\"")
            } else {
                document.remove(attr2.valueSegment)
                document.insert(attr2.valueSegment.begin, attr2.value + " ncms-block")
            }
            document.replace(blockAttr, "data-ncms-block=\"$!{ncmsVEBlockId('${blockAttr.value}')}\"")

            if (el.endTag == null) {
//                val st = el.startTag.toString()
//                if (st.length > 2) {
//                    document.replace(el.startTag.end - 2, el.startTag.end,
//                            ">#if(ncmsVEBlockExists('${blockAttr.value}'))$!{ncmsVEBlock('${blockAttr.value}')}#end</${el.name}>"
//                    )
//                }
            } else {
                document.insert(el.content.begin, "#if(ncmsVEBlockExists('${blockAttr.value}'))$!{ncmsVEBlock('${blockAttr.value}')}#else ")
                document.insert(el.content.end, " #end")
            }
        }
    }
}