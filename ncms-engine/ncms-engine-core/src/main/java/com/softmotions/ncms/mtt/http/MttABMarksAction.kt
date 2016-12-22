package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import com.softmotions.web.*
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


/**
 * Set A/B marks action.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
@Suppress("UNCHECKED_CAST")
class MttABMarksAction : MttActionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override val type = "abmarks"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {

        val spec = ctx.spec
        if (!ctx.containsKey("marks")) {
            synchronized(this) {
                if (ctx.containsKey("marks")) return@synchronized
                ctx["marks"] = spec.path("marks")
                        .asText()
                        .split(',')
                        .filter { it.isNotBlank() }.map { it.trim().toLowerCase() }
                        .toSet()
            }
        }

        val req = rmc.req
        val rid = (req.getAttribute(MttHttpFilter.MTT_RIDS_KEY) as Collection<Long>).last()
        val cookieName = "_abm_${rid}"

        if (req[cookieName] != null) {
            if (log.isDebugEnabled) {
                log.debug("Skipping AB cookie set due to existing req " +
                        "attribute: ${cookieName}=${req[cookieName]}")
            }
            rmc.paramsForRedirect[cookieName] = (req[cookieName] as Set<String>).joinToString(",");
            return false
        }

        val marks = ctx["marks"] as Set<String>
        var cookie = req.cookie(cookieName)
        if (cookie != null) {
            if (log.isDebugEnabled) {
                log.debug("Skipping AB cookie set due to existing " +
                        "cookie: ${cookieName}=${cookie.decodeValue()}")
            }
            rmc.paramsForRedirect[cookieName] = cookie.decodeValue();
            return false
        }

        val time = spec.path("time").asInt()
        val units = spec.path("units").asText("")
        if (units.isEmpty()) {
            log.error("Invalid action spec: ${spec}")
            return false
        }
        cookie = Cookie(cookieName, null)
        cookie.setEncodedValue(marks.joinToString(","));
        cookie.maxAge = when (units) {
            "days" -> time * 24 * 60 * 60
            "minutes" -> time * 60
            else -> {
                log.error("Invalid action units, spec=${spec}")
                time
            }
        }
        resp.addCookie(cookie);
        req[cookieName] = marks
        rmc.paramsForRedirect[cookieName] = cookie.decodeValue();
        if (log.isDebugEnabled) {
            log.debug("Set AB cookie ${cookieName}=${cookie.decodeValue()} " +
                    "maxAge=${cookie.maxAge}")
        }
        return false
    }
}