package com.softmotions.ncms.mtt

/**
 * Mtt rule update event
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
data class MttRuleUpdatedEvent(val ruleId: Long, val hint: String = "") {
}