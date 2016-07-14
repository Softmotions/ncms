package com.softmotions.ncms.marketing.mtt

import java.io.Serializable
import java.util.*

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
data class MttRuleFilter(var id: Long = 0,
                         var ruleId: Long? = null,
                         var type: String? = null,
                         var description: String? = null,
                         var spec: String? = null,
                         var cdate: Date? = null,
                         var mdate: Date? = null,
                         var enabled: Boolean? = true

) : Serializable {

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    constructor(id: java.lang.Long,
                ruleId: java.lang.Long,
                type: java.lang.String,
                description: java.lang.String,
                spec: java.lang.String,
                cdate: java.sql.Timestamp,
                mdate: java.sql.Timestamp,
                enabled: java.lang.Boolean)
    : this(id.toLong(), ruleId.toLong(), type.toString(), description.toString(),
            spec.toString(), Date(cdate.time), Date(mdate.time),
            enabled.booleanValue())
}
