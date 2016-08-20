package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.asm.CachedPage
import com.softmotions.ncms.asm.PageService
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
open class MttPageFilter
@Inject
constructor(private val ps: PageService) : MttFilterHandler {

    companion object {
        val log = LoggerFactory.getLogger(MttPageFilter::class.java)
    }

    override val type: String = "page"

    private val GUID_REGEXP: Regex = Regex("[0-9a-f]{32}")

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        val spec = ctx.spec
        var pageName = GUID_REGEXP.find(spec.path("pageref").asText(null))?.value ?: return false

        var uri = req.requestURI
        if (uri[0] == '/') {
            uri = uri.substring(1)
        }

        if (log.isDebugEnabled) {
            log.info("match='$pageName' req='${req.requestURI}'")
        }

        var cp = ps.getCachedPage(uri, true)
        if (cp == null) {
            if (uri.isNotBlank() && !"index.html".equals(uri)) {
                return false
            }
            cp = ps.getIndexPage(req, true)
        }
        return pageName.equals(cp?.name)
    }
}
