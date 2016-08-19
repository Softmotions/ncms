package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request page filter.
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
@Singleton
@ThreadSafe
open class MttPageFilter : MttFilterHandler {

    companion object {
        val log = LoggerFactory.getLogger(MttPageFilter::class.java)
    }

    override val type: String = "page"

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        val spec = ctx.spec
        var pageName = Regex("[0-9a-f]{32}").find(spec.path("pageref").asText())?.value;
        var uri = Regex("^/").replace(req.requestURI, "");

        if (log.isDebugEnabled) {
            log.info("match='$pageName' req='${req.requestURI}'")
        }

        // todo: get NAME from ASMS with NAME or NAV_ALIAS == uri
        // todo: return NAME == pageName
        log.info("pageName='$pageName' uri='$uri'")
        return uri == pageName // TODO FIXME
    }
}
