package com.softmotions.ncms.mtt.http

import com.softmotions.web.HttpUtils
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

/**
 * User agent mtt filter.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class MttUserAgentFilter : MttFilterHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override val type = "useragent"

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {

        val ua = req.getHeader("user-agent")?.toLowerCase() ?: return true
        val spec = ctx.spec

        if (log.isDebugEnabled) {
            log.debug("User agent: ${ua}")
            log.debug("Spec: ${spec}")
        }

        if (spec.path("desktop").asBoolean(false)) {
            if (ua.contains("mobile")
                    || ua.contains("android")
                    || ua.contains("iphone")
                    || ua.contains("ipad")
                    || ua.contains("ipod")
                    || ua.contains("arm;")) {
                if (log.isDebugEnabled) {
                    log.debug("desktop=false")
                }
                return false
            }
        }

        if (spec.path("mobile").asBoolean(false)) {
            if (!HttpUtils.isMobile(ua)) {
                if (log.isDebugEnabled) {
                    log.debug("mobile=false")
                }
                return false
            }
        }

        if (spec.path("tablet").asBoolean(false)) {
            if (!HttpUtils.isTablet(ua)) {
                if (log.isDebugEnabled) {
                    log.debug("tablet=false")
                }
                return false
            }
        }

        if (spec.path("android").asBoolean(false)) {
            if (!ua.contains("android")) {
                if (log.isDebugEnabled) {
                    log.debug("android=false")
                }
                return false
            }
        }

        if (spec.path("ios").asBoolean(false)) {
            if (!ua.contains("iphone") && !ua.contains("ipad") && !ua.contains("ipod")) {
                if (log.isDebugEnabled) {
                    log.debug("ios=false")
                }
                return false
            }
        }

        if (spec.path("osx").asBoolean(false)) {
            if (!ua.contains("mac os")) {
                if (log.isDebugEnabled) {
                    log.debug("osx=false")
                }
                return false;
            }
        }

        if (spec.path("windows").asBoolean(false)) {
            if (!ua.contains("windows")) {
                if (log.isDebugEnabled) {
                    log.debug("windows=false")
                }
                return false;
            }
        }

        if (spec.path("unix").asBoolean(false)) {
            if (!ua.contains("unix") && !ua.contains("linux") && !ua.contains("x11")) {
                if (log.isDebugEnabled) {
                    log.debug("unix=false")
                }
                return false
            }
        }

        if (spec.path("webkit").asBoolean(false)) {
            if (!HttpUtils.isWebkit(ua)) {
                if (log.isDebugEnabled) {
                    log.debug("webkit=false")
                }
                return false
            }
        }

        if (spec.path("gecko").asBoolean(false)) {
            if (!HttpUtils.isGecko(ua)) {
                if (log.isDebugEnabled) {
                    log.debug("gecko=false")
                }
                return false
            }
        }

        if (spec.path("trident").asBoolean(false)) {
            if (!HttpUtils.isTrident(ua)) {
                if (log.isDebugEnabled) {
                    log.debug("trident=false")
                }
                return false
            }
        }

        if (spec.path("edge").asBoolean(false)) {
            if (!HttpUtils.isEdge(ua)) {
                if (log.isDebugEnabled) {
                    log.debug("edge=false")
                }
                return false
            }
        }

        if (log.isDebugEnabled) {
            log.debug("User agent test passed")
        }
        return true
    }
}