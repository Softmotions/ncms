package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.NcmsEnvironment
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
@JvmSuppressWildcards
open class MttPageFilter
@Inject
constructor(private val ps: PageService,
            val env: NcmsEnvironment) : MttFilterHandler {

    companion object {
        private val log = LoggerFactory.getLogger(MttPageFilter::class.java)
    }

    override val type: String = "page"

    private val PAGEREF_REGEXP = Regex("page:\\s*([0-9a-f]{32})(\\s*\\|.*)?", RegexOption.IGNORE_CASE)

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        val spec = ctx.spec
        val pageref = spec.path("pageref").asText(null) ?: return false
        val mres = PAGEREF_REGEXP.matchEntire(pageref) ?: return false
        val pageName = mres.groupValues[1]
        val requestURI = req.requestURI
        val uri = requestURI.removePrefix(env.appRoot + "/")

        if (log.isDebugEnabled) {
            log.info("match='$pageName' req='$requestURI'")
        }

        if (pageName == uri) {
            return true // request page by guid
        }

        var cp = ps.getCachedPage(uri, true)
        if (cp == null) {
            if (uri.isNotBlank() && "index.html" != uri) {
                return false
            }
            cp = ps.getIndexPage(req, true)
        }
        return pageName == cp?.name
    }
}
