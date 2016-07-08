package com.softmotions.ncms.marketing.rules

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
class RuleAction {

    var id: Long = 0
    // todo: rule
    var name: String? = null
    var type: String? = null // may be enum?
    var ordinal: Int = 0
    var spec: String? = null
}
