package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.NcmsEnvironment
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Route rule handler
 */
@Singleton
open class MttRouteActionHandler
@Inject
constructor(val env: NcmsEnvironment) : MttActionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(MttRouteActionHandler::class.java)
    }

    private val PAGEREF_REGEXP = Regex("page:\\s*([0-9a-f]{32})(\\s*\\|.*)?", RegexOption.IGNORE_CASE)

    override val type: String = "route"

    override fun execute(ctx: MttActionHandlerContext,
                         req: HttpServletRequest,
                         resp: HttpServletResponse): Boolean {

        fun attachQueryParams(url: String): String {
            if (StringUtils.isBlank(req.queryString)) {
                return url;
            }
            if ("?" in url) {
                return url + "&" + req.queryString
            } else {
                return url + "?" + req.queryString
            }
        }

        fun redirect(target: String) {
            if (log.isDebugEnabled) {
                log.debug("Send redirect: ${attachQueryParams(target)}")
            }
            return resp.sendRedirect(attachQueryParams(target))
        }

        fun forward(target: String) {
            if (log.isDebugEnabled) {
                log.debug("Forwarding to: ${target}")
            }
            req.getRequestDispatcher(target).forward(req, resp)
        }

        val target = ctx.spec.path("target").asText()
        if (target.isEmpty()) {
            if (log.isWarnEnabled) {
                log.warn("No target in spec")
            }
            return false
        }
        val mres = PAGEREF_REGEXP.matchEntire(target)
        if (mres != null) {  // Forward to internal page
            val guid = mres.groupValues[1]
            forward("/${guid}")
        } else {
            if ("://" in target) {
                redirect(target)
            } else if (target.startsWith("//")) {
                redirect(req.scheme + "://" + target)
            } else if (!target.isEmpty() && target[0] != '/') {
                forward('/' + target)
            } else {
                forward(target)
            }
        }
        return true
    }
}