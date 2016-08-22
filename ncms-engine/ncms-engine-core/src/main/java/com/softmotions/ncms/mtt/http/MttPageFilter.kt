package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.commons.re.RegexpHelper
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
open class MttPageFilter
@Inject
constructor(private val ps: PageService,
            val env: NcmsEnvironment) : MttFilterHandler {

    companion object {
        val log = LoggerFactory.getLogger(MttPageFilter::class.java)
    }

    override val type: String = "page"

    private val PAGEREF_REGEXP = Regex("page:\\s*([0-9a-f]{32})(\\s*\\|.*)?", RegexOption.IGNORE_CASE)
    private val APPPREFIX_REGEXP = Regex(RegexpHelper.convertGlobToRegEx(env.appRoot)
            + if ("/".equals(env.appRoot)) { "" } else { "/" })

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        val spec = ctx.spec
        val mres = PAGEREF_REGEXP.matchEntire(spec.path("pageref").asText(null)) ?: return false
        val pageName = mres.groupValues[1]
        var uri = APPPREFIX_REGEXP.replaceFirst(req.requestURI, "")

        if (log.isDebugEnabled) {
            log.info("match='$pageName' req='${req.requestURI}'")
        }

        log.info("match='$pageName' req='${req.requestURI}' req='$uri'")
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
