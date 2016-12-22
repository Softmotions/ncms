package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request parameters filter.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
open class MttParametersFilter : AbstractMttParametersFilter() {

    override val type: String = "params"

    override fun createMSlot(name: String, required: Boolean, negate: Boolean): MSlot {
        return ParamsMSlot(name, required, negate)
    }

    class ParamsMSlot(name: String, required: Boolean, negate: Boolean)
    : MSlot(name, required, negate) {
        override fun getValues(req: HttpServletRequest): Array<String> {
            return req.getParameterValues(name) ?: emptyArray()
        }
    }
}