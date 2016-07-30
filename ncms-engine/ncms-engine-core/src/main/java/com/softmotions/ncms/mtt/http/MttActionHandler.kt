package com.softmotions.ncms.mtt.http

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface MttActionHandler {

    val type: String

    /**
     * @return `True` if the response handled by this action
     */
    fun execute(ctx: MttActionHandlerContext, req: HttpServletRequest, resp: HttpServletResponse): Boolean
}