package com.softmotions.ncms.mtt.http

import com.google.inject.Singleton
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request cookies filter.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@ThreadSafe
class MttCookiesFilter : AbstractMttParametersFilter() {

    override val type: String = "cookies"

    override fun createMSlot(name: String, required: Boolean): MSlot {
        return CookiesMSlot(name, required)
    }

    class CookiesMSlot(name: String, required: Boolean) : MSlot(name.toLowerCase(), required) {

        override fun getValues(req: HttpServletRequest): Array<String> {
            return req.cookies.filter {
                it.name == name
            }.map {
                it.value
            }.toTypedArray()
        }
    }

}