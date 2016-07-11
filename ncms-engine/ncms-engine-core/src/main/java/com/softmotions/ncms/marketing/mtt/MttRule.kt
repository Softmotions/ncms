package com.softmotions.ncms.marketing.mtt

import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*

/**
 * Правило фильтрации входящих запросов
 * и контекстное выполнение действий
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
data class MttRule(var id: Long = 0,
                   var name: String? = null,
                   var ordinal: Long? = null, // в правилах может быть важен порядок
                   var cdate: Date? = null,
                   var mdate: Date? = null,
                   var flags: Long = 0 // флаги режима работы правила
) : Serializable {

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    constructor(id: java.lang.Long,
                name: java.lang.String,
                ordinal: java.lang.Long,
                cdate: java.sql.Timestamp,
                mdate: java.sql.Timestamp,
                flags: java.lang.Long)
    : this(id.toLong(), name.toString(), ordinal.toLong(), Date(cdate.time), Date(mdate.time), flags.toLong()) {
    }

    companion object {
        private val log = LoggerFactory.getLogger(MttRule::class.java)
    }
}
