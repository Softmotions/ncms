package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.NcmsEnvironment
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request prefix filter.
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
@Singleton
@ThreadSafe
open class MttPrefixFilter
@Inject
constructor(val env: NcmsEnvironment) : MttFilterHandler {

    companion object {
        private val log = LoggerFactory.getLogger(MttPrefixFilter::class.java)
    }

    override val type: String = "prefix"

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        val spec = ctx.spec
        val appPrefix = env.appRoot + if ("/".equals(env.appRoot)) { "" } else { "/" }
        val prefixRaw = spec.path("prefix").asText(null) ?: return false
        val prefix = if (prefixRaw.startsWith(appPrefix)) { prefixRaw.substring(appPrefix.length) } else { prefixRaw }
        val uri = if (req.requestURI.startsWith(appPrefix)) { req.requestURI.substring(appPrefix.length) } else { req.requestURI }

        if (log.isDebugEnabled) {
            log.info("prefix='$prefixRaw' req='${req.requestURI}'")
        }

        return uri.startsWith(prefix)
    }
}
