package com.softmotions.ncms.marketing.mtt

import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
data class MttRuleAction(var id: Long = 0,
                         var ruleId: Long? = null,
                         var ordinal: Long? = null,
                         var type: String? = null, // may be enum?
                         var description: String? = null,
                         var spec: String? = null,
                         var cdate: Date? = null,
                         var mdate: Date? = null
) : Serializable {

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    constructor(id: java.lang.Long,
                ruleId: java.lang.Long,
                ordinal: java.lang.Long,
                type: java.lang.String,
                description: java.lang.String,
                spec: java.lang.String,
                cdate: java.sql.Timestamp,
                mdate: java.sql.Timestamp)
    : this(id.toLong(), ruleId.toLong(), ordinal.toLong(), type.toString(), description.toString(), spec.toString(), Date(cdate.time), Date(mdate.time))

    companion object {
        private val log = LoggerFactory.getLogger(MttRuleAction::class.java)
    }
}
