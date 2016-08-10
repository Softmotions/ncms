package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import com.softmotions.commons.cont.KVOptions
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletResponse

/**
 * Set A/B marks action.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@ThreadSafe
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
                ctx["marks"] = KVOptions(spec.path("marks").asText())
            }
        }
        val marks = ctx["marks"] as KVOptions
        val units = spec.path("units").asText()
        if (units.isEmpty()) {
            log.error("Invalid action spec: ${spec}")
            return false
        }

        fun toMaxAge(time: Int, units: String): Int {
            return when (units) {
                "days" -> time * 24 * 60 * 60
                "minutes" -> time * 60
                else -> {
                    log.error("Invalid action units, spec=${spec}")
                    time
                }
            }
        }

        //todo

        return false
    }
}