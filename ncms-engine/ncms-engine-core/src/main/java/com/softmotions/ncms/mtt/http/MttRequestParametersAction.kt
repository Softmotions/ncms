package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import com.softmotions.commons.cont.KVOptions
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletResponse

/**
 * Set request parameters action handler
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
class MttRequestParametersAction : MttActionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override val type: String = "parameters"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {

        val overwrite = ctx.spec.path("overwrite").asBoolean()
        if (!ctx.containsKey("pmap")) {
            synchronized(this) {
                if (ctx.containsKey("pmap")) return@synchronized
                // {"overwrite":true,"params":"foo=bar"}
                ctx["pmap"] = KVOptions(ctx.spec.path("params").asText(""))
            }
        }
        @Suppress("UNCHECKED_CAST")
        (ctx["pmap"] as Map<String, String>).forEach {
            val opv = rmc.req.getParameter(it.key)
            if (overwrite || opv == null) {
                if (log.isDebugEnabled) {
                    log.debug("Setup request parameter ${it.key}=${it.value}")
                }
                rmc.params.put(it.key, it.value)
            }
        }
        return false
    }
}