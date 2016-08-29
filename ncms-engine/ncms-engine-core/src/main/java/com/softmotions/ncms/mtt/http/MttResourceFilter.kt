package com.softmotions.ncms.mtt.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.NcmsEnvironment
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.http.HttpServletRequest

/**
 * Request prefix filter.
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
@Singleton
@ThreadSafe
@JvmSuppressWildcards
open class MttResourceFilter
@Inject
constructor(val env: NcmsEnvironment) : MttFilterHandler {

    companion object {
        private val log = LoggerFactory.getLogger(MttResourceFilter::class.java)
    }

    override val type: String = "resource"

    fun removeURIPrefix(uri: String, prefix: String): String {
        var uriWSlashes = uri
        if (!uriWSlashes.startsWith("/")) {
            uriWSlashes = "/" + uri
        }
        if (!uriWSlashes.endsWith("/")) {
            uriWSlashes += "/"
        }
        return uriWSlashes.removePrefix(prefix)
    }

    override fun matched(ctx: MttFilterHandlerContext, req: HttpServletRequest): Boolean {
        val spec = ctx.spec
        val appPrefix = env.appRoot + "/"
        val prefixRaw = spec.path("prefix").asText(null) ?: return false

        val prefix = removeURIPrefix(prefixRaw, appPrefix)
        val uri = removeURIPrefix(req.requestURI, appPrefix)

        if (log.isDebugEnabled) {
            log.info("prefix='$prefixRaw' req='${req.requestURI}'")
        }

        return uri.startsWith(prefix)
    }
}
