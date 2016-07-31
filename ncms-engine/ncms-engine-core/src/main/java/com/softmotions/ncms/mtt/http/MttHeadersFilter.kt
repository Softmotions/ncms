package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request headers filter.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@ThreadSafe
open class MttHeadersFilter : AbstractMttParametersFilter() {

    override val type: String = "headers"

    override fun createMSlot(name: String, required: Boolean): MSlot {
        return HeadersMSlot(name, required)
    }

    class HeadersMSlot(name: String, required: Boolean) : MSlot(name.toLowerCase(), required) {

        override fun getValues(req: HttpServletRequest): Array<String> {
            val henum = req.getHeaders(name) ?: return emptyArray()
            val first = if (henum.hasMoreElements()) henum.nextElement() else return emptyArray()
            val second = if (henum.hasMoreElements()) henum.nextElement() else return arrayOf(first)
            return arrayOf(first, second, *henum.toList().toTypedArray())
        }
    }
}