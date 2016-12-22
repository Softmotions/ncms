package com.softmotions.ncms.mtt.http

import com.softmotions.commons.string.EscapeHelper
import org.apache.commons.collections4.iterators.IteratorEnumeration
import org.apache.commons.collections4.map.Flat3Map
import org.slf4j.LoggerFactory
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class MttRequestModificationContext(val req: HttpServletRequest) {

    private val log = LoggerFactory.getLogger(javaClass)

    val params = Flat3Map<String, String>()

    val paramsForRedirect by lazy {
        Flat3Map<String, String>()
    }

    fun paramsAsQueryString(): String? {
        var qs = req.queryString ?: ""
        if (params.isNotEmpty()) {
            qs += params.entries.map {
                "${EscapeHelper.encodeURLComponent(it.key)}=${EscapeHelper.encodeURLComponent(it.value)}"
            }.joinToString(separator = "&", prefix = (if (qs.isNotEmpty()) "&" else ""))
        }
        return if (qs.isEmpty()) null else qs
    }

    fun applyModifications(): HttpServletRequest {
        if (params.isEmpty) {
            return req
        }
        // create request wrapper
        return object : HttpServletRequestWrapper(req) {
            private val qs = paramsAsQueryString()
            private val pmap = HashMap<String, Array<out String>>().apply {
                putAll(req.parameterMap)
                params.forEach {
                    this.put(it.key, arrayOf(it.value))
                }
            }

            override fun getQueryString(): String? {
                return qs
            }

            override fun getParameterMap(): Map<String, Array<out String>> {
                return pmap
            }

            override fun getParameterValues(name: String): Array<out String> {
                return pmap[name] ?: emptyArray()
            }

            override fun getParameterNames(): Enumeration<String> {
                return IteratorEnumeration<String>(params.keys.iterator())
            }

            override fun getParameter(name: String): String? {
                return getParameterValues(name).let {
                    it.firstOrNull()
                }
            }
        }
    }

    private fun attachQueryParams(url: String): String {
        val qs = paramsAsQueryString() ?: return url
        if ("?" in url) {
            return url + "&" + qs
        } else {
            return url + "?" + qs
        }
    }

    fun redirect(target: String, resp: HttpServletResponse) {
        if (log.isDebugEnabled) {
            log.debug("Send redirect: ${attachQueryParams(target)}")
        }
        if (paramsForRedirect.isNotEmpty()) {
            params += paramsForRedirect
        }
        return resp.sendRedirect(attachQueryParams(target))
    }

    fun forward(target: String, resp: HttpServletResponse) {
        if (log.isDebugEnabled) {
            log.debug("Forwarding to: ${target}")
        }
        req.getRequestDispatcher(target).forward(applyModifications(), resp)
    }
}