package com.softmotions.ncms.mtt.tp

/**
 * Tracking pixel updated event.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
data class MttTpUpdatedEvent(val tpId: Long, val hint: String = "")