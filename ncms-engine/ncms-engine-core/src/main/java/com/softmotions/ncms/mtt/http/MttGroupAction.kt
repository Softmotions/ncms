package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
open class MttGroupAction : MttActionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override val type: String = "group"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {
        val glist = ctx.findGroupActions();
        if (log.isDebugEnabled) {
            log.debug("Groups={}",
                    glist.map { it.action }
            )
        }
        var sum = 0.0
        for (ac in glist) {
            sum += ac.action.groupWeight
        }
        if (sum == 0.0) { // zero width
            return false
        }
        val p = Math.random() * sum
        var cp = 0.0
        if (log.isDebugEnabled) {
            log.debug("P=${p}")
        }
        for (ac in glist) {
            cp += ac.action.groupWeight
            if (p <= cp) {
                if (log.isDebugEnabled) {
                    log.debug("Executing: ${cp}")
                }
                return ac.execute(rmc, resp)
            }
        }
        return false
    }
}