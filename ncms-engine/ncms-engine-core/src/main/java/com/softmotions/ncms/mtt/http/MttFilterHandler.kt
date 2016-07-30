package com.softmotions.ncms.mtt.http

import javax.servlet.http.HttpServletRequest

interface MttFilterHandler {

    val type: String

    /**
     * @return `True` if request matched this filter
     */
    fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean
}