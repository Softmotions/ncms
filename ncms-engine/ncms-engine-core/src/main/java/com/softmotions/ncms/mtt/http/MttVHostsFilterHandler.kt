package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import com.softmotions.commons.re.RegexpHelper
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

@Singleton
open class MttVHostsFilterHandler : MttFilterHandler {

    companion object {
        val log = LoggerFactory.getLogger(MttVHostsFilterHandler::class.java)
    }

    override val type: String = "vhosts"

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        if (!ctx.contains("pattern")) {
            val spec = ctx.spec
            if (spec.path("regexp").isTextual) {
                ctx["pattern"] = Regex(spec.path("regexp").asText())
            } else if (spec.path("glob").isTextual) {
                ctx["pattern"] = Regex(RegexpHelper.convertGlobToRegEx(spec.path("glob").asText()))
            } else {
                log.error("Invalid filter spec: ${spec}")
                return false
            }
        }
        return (ctx["pattern"] as Regex).matches(req.serverName)
    }
}