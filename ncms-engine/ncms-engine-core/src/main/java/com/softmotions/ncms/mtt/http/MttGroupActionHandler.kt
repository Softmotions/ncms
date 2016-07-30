package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
open class MttGroupActionHandler : MttActionHandler {

    override val type: String = "group"

    override fun execute(ctx: MttActionHandlerContext,
                         req: HttpServletRequest,
                         resp: HttpServletResponse): Boolean {
        val glist = ctx.findGroupActions();
        var sum = 0.0
        for (ac in glist) {
            sum += ac.action.groupWeight
        }
        if (sum == 0.0) { // zero width
            return false
        }
        val p = Math.random() * sum
        var cp = 0.0
        for (ac in glist) {
            cp += ac.action.groupWeight
            if (p <= cp) {
                return ac.execute(req, resp)
            }
        }
        return false
    }
}