package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.mtt.tp.MttTpService
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletResponse

/**
 * Remember request origin action.
 * Tracking pixels data is also remembered.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
@JvmSuppressWildcards
class MttRememberOriginAction
@Inject
constructor(val tps: MttTpService) : MttActionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override val type: String = "remember"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {

        val spec = ctx.spec
        if (spec.path("tp").asBoolean()) {
            if (log.isDebugEnabled) {
                log.debug("Injecting tracking pixels")
            }
            tps.injectTrackingPixels(rmc.req, resp)
        }

        return false
    }
}