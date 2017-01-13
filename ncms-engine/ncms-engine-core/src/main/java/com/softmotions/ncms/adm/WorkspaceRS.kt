package com.softmotions.ncms.adm

import com.google.inject.Inject
import com.softmotions.commons.lifecycle.Start
import com.softmotions.ncms.NcmsEnvironment
import com.softmotions.ncms.asm.PageService
import com.softmotions.web.security.WSUser
import com.softmotions.weboot.security.WBSecurityContext
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.*
import javax.ws.rs.core.Context

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Path("adm/ws")
@Produces("application/json;charset=UTF-8")
@JvmSuppressWildcards
open class WorkspaceRS
@Inject
constructor(private val env: NcmsEnvironment,
            private val pageService: PageService,
            private val sctx: WBSecurityContext) {

    private val log = LoggerFactory.getLogger(WorkspaceRS::class.java)

    private val helpTopics = HashMap<String, String?>()

    @Start
    open fun start() {
        val topics = env.xcfg().configurationsAt("help.topics.topic")
        for (topic in topics) {
            val key = topic.getString("[@key]")
            if (StringUtils.isBlank(key)) {
                continue
            }
            val alias = topic.getString("[@alias]")
            if (!StringUtils.isBlank(alias)) {
                helpTopics.put(key, pageService.resolvePageLink(alias))
            } else {
                helpTopics.put(key, topic.getString("")) //get config tag content
            }
        }
        if ("wiki.gmap" !in helpTopics) {
            helpTopics["wiki.gmap"] = "https://support.google.com/maps/answer/3544418"
        }
        if ("wiki" !in helpTopics) {
            helpTopics["wiki"] = "http://ncms.one/manual/doc/ui/am/wiki/wiki.html"
        }
    }

    @GET
    @Path("state")
    @Throws(Exception::class)
    open fun state(@Context req: HttpServletRequest,
                   @Context resp: HttpServletResponse): WSUserState {
        return WSUserState(sctx.getWSUser(req), req)
    }

    @PUT
    @Path("state")
    open fun saveState(props: Map<String, Any>) {
        log.info("Save state: {}", props)
        //todo
    }

    @PUT
    @Path("state/{property}")
    open fun saveStateProperty(@PathParam("property") property: String, value: String) {
        log.info("Save state[ {}] = {}", property, value)
        //todo
    }

    @GET
    @Path("logout")
    open fun logout(@Context req: HttpServletRequest,
                    @Context resp: HttpServletResponse) {
        val sess = req.getSession(false)
        sess?.invalidate()
        try {
            resp.sendRedirect(env.logoutRedirect)
        } catch (e: IOException) {
            log.error("", e)
        }
    }

    inner class WSUserState(user: WSUser, req: HttpServletRequest) : HashMap<String, Any?>() {
        internal val properties: Map<String, Any?> = HashMap<String, Any?>().apply {
            put("max-edit-text-size", env.xcfg().getInt("media.max-edit-text-size", 1048576))
        }
        init {
            put("appName", env.applicationName)
            put("sessionId", req.session.id)
            put("userId", user.name)
            put("userLogin", user.name)
            put("userFullName", user.fullName)
            put("roles", user.roleNames)
            put("email", user.email)
            put("time", Date())
            put("helpSite", helpSite)
            put("properties", properties)
            put("helpTopics", getHelpTopics())
            put("serverTZOffset", TimeZone.getDefault().rawOffset)

        }
    }

    private val helpSite: String? by lazy {
        val alias = env.xcfg().getString("help.site[@alias]")
        if (!StringUtils.isBlank(alias)) {
            return@lazy pageService.resolvePageLink(alias)
        } else {
            return@lazy env.xcfg().getString("help.site", "http://ncms.one/manual")
        }
    }

    private fun getHelpTopics(): Map<String, String?> {
        return helpTopics
    }
}
