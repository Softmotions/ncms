package com.softmotions.ncms.mtt.tp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.Inject
import com.softmotions.ncms.events.NcmsEventBus
import com.softmotions.ncms.jaxrs.NcmsMessageException
import com.softmotions.weboot.i18n.I18n
import com.softmotions.weboot.mb.MBCriteriaQuery
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.commons.lang3.StringUtils
import org.apache.ibatis.session.SqlSession
import org.apache.shiro.authz.annotation.RequiresRoles
import org.mybatis.guice.transactional.Transactional
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.StreamingOutput

/**
 * MTT Tracking pixels support
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Path("adm/mtt/tp")
@Produces("application/json;charset=UTF-8")
@JvmSuppressWildcards
open class MttTpRS
@Inject
constructor(val sess: SqlSession,
            val mapper: ObjectMapper,
            val i18n: I18n,
            val ebus: NcmsEventBus) : MBDAOSupport(MttTpRS::class.java, sess) {

    @GET
    @Path("/select")
    @RequiresRoles("mtt")
    open fun select(@Context req: HttpServletRequest): Response =
            Response.ok(StreamingOutput {
                with(mapper.factory.createGenerator(it)) {
                    writeStartArray()
                    selectByCriteria(createTpQ(req), { ctx ->
                        @Suppress("UNCHECKED_CAST")
                        writeObject((ctx.resultObject as Map<String, Any>).mapValues {
                            when {
                            // convert enabled field to boolean
                                it.key == "enabled" && it.value is Number -> it.value as Number != 0
                                else -> it.value
                            }
                        })
                    }, "select")
                    writeEndArray()
                    flush()
                }
            })
                    .type("application/json;charset=UTF-8")
                    .build()

    @GET
    @Path("/select/count")
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    open fun count(@Context req: HttpServletRequest): Long =
            selectOneByCriteria(createTpQ(req), "count") ?: 0L

    @GET
    @Path("/tp/{id}")
    @RequiresRoles("mtt")
    @Transactional
    open fun tpGet(@PathParam("id") id: Long): MttTp =
            selectOne("selectTpById", id) ?: throw NotFoundException()

    @PUT
    @Path("/tp/{name}")
    @RequiresRoles("mtt")
    @Transactional
    open fun tpCreate(@Context req: HttpServletRequest,
                      @PathParam("name") name: String): MttTp {

        synchronized(MttTp::class.java) {
            val rname = name.trim();
            if (selectOne<Long?>("selectTpIdByName", rname) != null) {
                throw NcmsMessageException(
                        i18n.get("ncms.mtt.tp.name.already.other", req, rname), true)
            }
            val tp = MttTp(rname)
            insert("insertTp", tp)
            checkNotNull(tp.id)
            ebus.fireOnSuccessCommit(MttTpUpdatedEvent(tp.id))
            return tp
        }
    }

    @PUT
    @Path("/tp/rename/{id}/{name}")
    @RequiresRoles("mtt")
    @Transactional
    open fun tpRename(@Context req: HttpServletRequest,
                      @PathParam("id") id: Long,
                      @PathParam("name") name: String) {

        synchronized(MttTp::class.java) {
            val rname = name.trim()
            if (selectOne<Long?>("selectTpIdByName", rname) != null) {
                throw NcmsMessageException(i18n.get("ncms.mtt.tp.name.already.other", req, rname), true)
            }
            update("updateTpName",
                    "id", id,
                    "name", name)
            ebus.fireOnSuccessCommit(MttTpUpdatedEvent(id))
        }
    }

    @POST
    @Path("/tp/{id}")
    @RequiresRoles("mtt")
    @Transactional
    open fun tpUpdate(@Context req: HttpServletRequest,
                      @PathParam("id") id: Long,
                      data: ObjectNode): Response {
        val tp = tpGet(id)
        if (data.hasNonNull("description")) {
            tp.description = data.path("description").asText("")
        }
        if (data.hasNonNull("enabled")) {
            tp.isEnabled = data.path("enabled").asBoolean(true)
        }
        data.remove(arrayListOf("description", "enabled"));
        tp.spec = mapper.writeValueAsString(data)
        update("updateTp", tp)
        ebus.fireOnSuccessCommit(MttTpUpdatedEvent(tp.id))

        val msg = NcmsMessageException()
        msg.addMessage(i18n.get("ncms.successfully.saved", req), false)
        return msg.injectNotification(Response.ok(mapper.writeValueAsString(tp)), i18n).build()
    }

    @DELETE
    @Path("/tp/{id}")
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun tpDelete(@PathParam("id") id: Long): Int {
        ebus.fireOnSuccessCommit(MttTpDeletedEvent(id))
        return delete("deleteTp", id)
    }

    @POST
    @Path("/tp/{id}/enable")
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun tpEnable(@PathParam("id") id: Long): Int {
        ebus.fireOnSuccessCommit(MttTpUpdatedEvent(id))
        return update("updateTpEnabled",
                "id", id,
                "enabled", true)
    }

    @POST
    @Path("/tp/{id}/disable")
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun tpDisable(@PathParam("id") id: Long): Int {
        ebus.fireOnSuccessCommit(MttTpUpdatedEvent(id))
        return update("updateTpEnabled",
                "id", id,
                "enabled", false)
    }

    private fun createTpQ(req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        val pv: String?
        pv = req.getParameter("stext")
        if (!StringUtils.isBlank(pv)) {
            cq.put("name", "${pv.trim().toLowerCase()}%")
        }
        initCriteriaPaging(cq, req)
        cq.orderBy("name")
        return cq
    }

    private fun initCriteriaPaging(cq: MBCriteriaQuery<MBCriteriaQuery<*>>, req: HttpServletRequest) {
        var pv: String? = req.getParameter("firstRow")
        if (pv != null) {
            val from = Integer.valueOf(pv)
            cq.offset(from!!)
            pv = req.getParameter("lastRow")
            if (pv != null) {
                val lrow = Integer.valueOf(pv)
                cq.limit(Math.abs(from - lrow!!) + 1)
            }
        }
    }
}