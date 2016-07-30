package com.softmotions.ncms.mtt.http

import com.fasterxml.jackson.databind.node.ObjectNode
import com.softmotions.ncms.mtt.MttRule
import com.softmotions.ncms.mtt.MttRuleFilter

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
interface MttFilterHandlerContext : MutableMap<String, Any?> {

    val spec: ObjectNode

    val rule: MttRule

    val filter: MttRuleFilter
}