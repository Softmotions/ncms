package com.softmotions.ncms.marketing.rules

import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
class Rule : Serializable {

    var id: Long = 0
    var name: String? = null
    var cdate: Date? = null
    var mdate: Date? = null

    companion object {
        private val log = LoggerFactory.getLogger(Rule::class.java)
    }
}
