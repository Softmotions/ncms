package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import com.softmotions.commons.re.RegexpHelper
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request parameters filter.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@ThreadSafe
open class MttParametersFilterHandler : MttFilterHandler {

    companion object {
        val log = LoggerFactory.getLogger(MttParametersFilterHandler::class.java)
    }

    override val type: String = "params"

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {

        if (!ctx.containsKey("mslots")) {
            synchronized(this) {
                if (ctx.containsKey("mslots")) return@synchronized
                val mslots = HashMap<String, MSlot>()
                val data = ctx.spec.path("data").asText()
                val lines = data.lines().filter {
                    !StringUtils.isBlank(it)
                }
                for (line in lines) {
                    val pairs = line.split("=", limit = 2).map {
                        it.trim()
                    }
                    if (pairs.size != 2) continue
                    val required = !pairs[0].endsWith("?")
                    val key = pairs[0].removeSuffix("?")
                    val value = pairs[1]
                    val mslot = mslots.getOrPut(key) {
                        createMSlot(key, required)
                    }
                    mslot.patterns += Regex(RegexpHelper.convertGlobToRegEx(value))
                }
                if (log.isDebugEnabled) {
                    mslots.values.forEach {
                        log.debug("Using matching slot: ${it}")
                    }
                }
                ctx["mslots"] = mslots.values
            }
        }

        @Suppress("UNCHECKED_CAST")
        val mslots = ctx["mslots"] as Collection<MSlot>
        for (mslot in mslots) {
            if (!mslot.matched(req)) {
                return false
            }
        }
        return true
    }

    open fun createMSlot(name: String, required: Boolean): MSlot {
        return MSlot(name, required)
    }

    open class MSlot(val name: String, val required: Boolean) {

        val patterns = ArrayList<Regex>()

        fun matched(req: HttpServletRequest): Boolean {
            val pvals = getValues(req)
            if (!required && pvals.isEmpty()) {
                return true
            }
            for (pv in pvals) {
                for (p in patterns) {
                    if (p.matches(pv)) {
                        return true
                    }
                }
            }
            return false
        }

        fun getValues(req: HttpServletRequest): Array<String> {
            return req.getParameterValues(name) ?: emptyArray()
        }

        override fun toString(): String {
            return "MSlot(name='$name', required=$required, patterns=$patterns)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false
            other as MSlot
            if (name != other.name) return false
            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}