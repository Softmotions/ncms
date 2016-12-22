package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import com.softmotions.web.cookie
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * Set cookie action.
 *
 * If cookie with same name was set previosly set
 * this action will be skipped
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
class MttCookieAction : MttActionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override val type = "cookie"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {
        val spec = ctx.spec
        val name = spec.path("name").asText("")
        val prev = rmc.req.cookie(name)
        if (prev != null) {
            return false.apply {
                if (log.isDebugEnabled) {
                    log.debug("Found cookie with same name: ${name}")
                }
            }
        }
        val value = spec.path("value").asText("")
        val time = spec.path("time").asInt()
        val units = spec.path("units").asText()
        if (value.isEmpty() || units.isEmpty()) {
            log.error("Invalid action spec: ${spec}")
            return false
        }
        val cookie = Cookie(name, value)
        if (time > 0) {
            cookie.maxAge = when (units) {
                "days" -> time * 24 * 60 * 60
                "minutes" -> time * 60
                else -> {
                    log.error("Invalid action units, spec=${spec}")
                    time
                }
            }
        }
        if (log.isDebugEnabled) {
            log.debug("Add cookie: ${cookie.name}=${cookie.value} maxAge=${cookie.maxAge}")
        }
        resp.addCookie(cookie)
        return false
    }
}