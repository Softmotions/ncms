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

    private val LEADING_SLASH_REGEXP: Regex = Regex("^/")

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        val spec = ctx.spec
        var pageName = GUID_REGEXP.find(spec.path("pageref").asText(""))?.value;
        if (pageName.isNullOrBlank()) {
            return false
        }
        var uri = LEADING_SLASH_REGEXP.replace(req.requestURI, "");

        if (log.isDebugEnabled) {
            log.info("match='$pageName' req='${req.requestURI}'")
        }

        var cp: CachedPage? = ps.getCachedPage(uri, true);
        return (cp != null) && (pageName.equals(cp.name))
    }
}
