package com.softmotions.ncms.marketing.mtt

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
class MttRuleAction {

    var id: Long = 0
    // todo: rule
    var name: String? = null
    var type: String? = null // may be enum?
    var ordinal: Int = 0
    var spec: String? = null
}
