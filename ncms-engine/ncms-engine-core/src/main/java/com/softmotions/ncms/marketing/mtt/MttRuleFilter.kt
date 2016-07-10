package com.softmotions.ncms.marketing.mtt

import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
// todo: rule
class MttRuleFilter(var id: Long = 0,
                    var type: String? = null,
                    var description: String? = null,
                    var spec: String? = null,
                    var cdate: Date? = null,
                    var mdate: Date? = null) {

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    constructor(id: java.lang.Long,
                type: java.lang.String,
                description: java.lang.String,
                spec: java.lang.String,
                cdate: java.sql.Timestamp,
                mdate: java.sql.Timestamp)
    : this(id.toLong(), type.toString(), description.toString(), spec.toString(), Date(cdate.time), Date(mdate.time))

    companion object {
        private val log = LoggerFactory.getLogger(MttRuleFilter::class.java)
    }
}
