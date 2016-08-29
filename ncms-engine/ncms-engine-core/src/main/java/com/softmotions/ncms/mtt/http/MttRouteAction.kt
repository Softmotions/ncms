package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.NcmsEnvironment
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletResponse

/**
 * Route rule handler
 */
@Singleton
@ThreadSafe
@JvmSuppressWildcards
open class MttRouteAction
@Inject
constructor(val env: NcmsEnvironment) : MttActionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(MttRouteAction::class.java)
    }

    private val PAGEREF_REGEXP = Regex("page:\\s*([0-9a-f]{32})(\\s*\\|.*)?", RegexOption.IGNORE_CASE)

    override val type: String = "route"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {
        val req = rmc.req
        val target = ctx.spec.path("target").asText("")
        if (target.isEmpty()) {
            if (log.isWarnEnabled) {
                log.warn("No target in spec")
            }
            return false
        }
        val appPrefix = env.appRoot +  "/"
        val mres = PAGEREF_REGEXP.matchEntire(target)
        if (mres != null) {  // Forward to internal page
            val guid = mres.groupValues[1]
            rmc.forward(appPrefix + guid, resp)
        } else {
            if ("://" in target) {
                rmc.redirect(target, resp)
            } else if (target.startsWith("//")) {
                rmc.redirect(req.scheme + ":" + target, resp)
            } else if (!target.startsWith(appPrefix)) {
                rmc.forward(appPrefix + target, resp)
            } else {
                rmc.forward(target, resp)
            }
        }
        return true
    }
}