package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import com.softmotions.commons.re.RegexpHelper
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

@Singleton
@ThreadSafe
open class MttVHostsFilter : MttFilterHandler {

    companion object {
        val log = LoggerFactory.getLogger(MttVHostsFilter::class.java)
    }

    override val type: String = "vhosts"

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        if (!ctx.contains("pattern")) {
            synchronized(this) {
                if (ctx.contains("pattern")) return@synchronized
                val spec = ctx.spec
                val mode = spec.path("mode").asText("")
                val pattern = spec.path("pattern").asText("")
                if (mode == "regexp") {
                    ctx["pattern"] = Regex(pattern)
                } else if (mode == "glob") {
                    ctx["pattern"] = Regex(RegexpHelper.convertGlobToRegEx(pattern))
                } else {
                    log.error("Invalid filter spec: ${spec}")
                    return false
                }
            }
        }
        val re = (ctx["pattern"] as Regex)
        if (log.isDebugEnabled) {
            log.debug("regexp='${re}' pattern='${req.serverName}' res=${re.matches(req.serverName)}")
        }
        return re.matches(req.serverName)
    }
}