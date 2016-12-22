package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletResponse

/**
 * Action what aggregates other actions.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
open class MttCompositeAction : MttActionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override val type: String = "composite"

    override fun execute(ctx: MttActionHandlerContext,
                         rmc: MttRequestModificationContext,
                         resp: HttpServletResponse): Boolean {

        val alist = ctx.findGroupActions();
        if (log.isDebugEnabled) {
            log.debug("Composite={}",
                    alist.map { it.action }
            )
        }
        for (a in alist) {
            if (a.execute(rmc, resp)) {
                return true
            }
        }
        return false
    }
}