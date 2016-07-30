package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Route rule handler
 */
@Singleton
open class MttRouteActionHandler : MttActionHandler {

    override val type: String = "route"

    override fun execute(ctx: MttActionHandlerContext,
                         req: HttpServletRequest,
                         resp: HttpServletResponse): Boolean {

        val spec = ctx.spec
        val target = spec.path("target").asText()
        if (target.isEmpty()) {
            return false
        }


        // {"target":"page:c2a177cf7ef8e61a075be90fd26ab584 | Лендинг2"}





        return false
    }
}