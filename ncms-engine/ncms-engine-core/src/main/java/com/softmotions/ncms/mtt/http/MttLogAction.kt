package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletResponse

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
open class MttLogAction : MttActionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(MttLogAction::class.java)
    }

    override val type: String = "log"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {
        val spec = ctx.spec
        val msg = spec.path("msg").asText(null) ?: return false
        val lvl = spec.path("level").asText("")
        when (lvl) {
            "ERROR" -> {
                log.error(msg)
            }
            "WARNING" -> {
                log.warn(msg)
            }
            "DEBUG" -> {
                log.debug(msg)
            }
            else -> {
                log.info(msg)
            }
        }
        return false
    }
}