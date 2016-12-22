package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request cookies filter.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@ThreadSafe
class MttCookiesFilter : AbstractMttParametersFilter() {

    override val type: String = "cookies"

    override fun createMSlot(name: String, required: Boolean, negate: Boolean): MSlot {
        return CookiesMSlot(name, required, negate)
    }

    class CookiesMSlot(name: String, required: Boolean, negate: Boolean)
    : MSlot(name.toLowerCase(), required, negate) {
        override fun getValues(req: HttpServletRequest): Array<String> {
            val cookies = req.cookies ?: return emptyArray()
            return cookies.filter {
                it.name == name
            }.map {
                it.value
            }.toTypedArray()
        }
    }
}