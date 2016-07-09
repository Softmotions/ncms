package com.softmotions.ncms.marketing.mtt

import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*

/**
 * Правило фильтрации входяхих запросов
 * и контекстное выполнение действий
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
class MttRule : Serializable {

    var id: Long = 0
    var name: String? = null
    var cdate: Date? = null
    var mdate: Date? = null
    var ordinal: Long = 0   // Все-таки в правилах может быть важен порядок
    var flags: Long = 0     // флаги режима работы правила

    companion object {
        private val log = LoggerFactory.getLogger(MttRule::class.java)
    }
}
