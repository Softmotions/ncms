package com.softmotions.ncms.adm

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.softmotions.ncms.NcmsEnvironment
import com.softmotions.weboot.i18n.I18n
import com.softmotions.weboot.security.WBSecurityContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context

/**
 * Accessible GUI resources configuration provider.

 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Path("adm/ui")
@Produces("application/json;charset=UTF-8")
@JvmSuppressWildcards
open class AdmUIResourcesRS
@Inject
constructor(
        private val env: NcmsEnvironment,
        private val mapper: ObjectMapper,
        private val msg: I18n,
        private val sctx: WBSecurityContext) {


    /**
     * List of qooxdoo widgets available for user.

     * @param section Section name widgets belongs to
     */
    @GET
    @Path("widgets/{section}")
    open fun parts(@Context req: HttpServletRequest,
                   @Context resp: HttpServletResponse,
                   @PathParam("section") section: String): JsonNode {

        // language fix todo review it
        req.getParameter("qxLocale")?.let {
            val cloc = msg.getLocale(req).toString()
            if (it != cloc) {
                msg.saveRequestLang(it, req, resp)
            }
        }

        val arr = mapper.createArrayNode()
        val user = sctx.getWSUser(req)
        val xcfg = env.xcfg()
        val cpath = "ui.$section.widget"
        for (hc in xcfg.configurationsAt(cpath)) {
            val widgetRoles = env.attrArray(hc.getString("[@roles]"))
            if (widgetRoles.size == 0 || user.isHasAnyRole(*widgetRoles)) {
                val qxClass = hc.getString("[@qxClass]")
                val icon = hc.getString("[@qxIcon]")
                val on = mapper.createObjectNode().put("qxClass", qxClass)
                val label = msg.get(qxClass + ".label", req)
                on.put("label", label)
                if (icon != null) {
                    on.put("icon", icon)
                }
                on.put("extra", hc.getBoolean("[@extra]", false))
                val argsNode = on.putArray("args")
                for (arg in env.attrArray(hc.getString("[@args]"))) {
                    argsNode.add(arg)
                }
                arr.add(on)
            }
        }
        return arr
    }
}
