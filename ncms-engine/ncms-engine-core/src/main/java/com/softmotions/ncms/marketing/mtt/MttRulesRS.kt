package com.softmotions.ncms.marketing.mtt

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.softmotions.weboot.mb.MBCriteriaQuery
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.ibatis.session.SqlSession
import org.mybatis.guice.transactional.Transactional
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.StreamingOutput

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */

@Path("adm/mtt/rules")
@Produces("application/json;charset=UTF-8")
open class MttRulesRS
@Inject
constructor(sess: SqlSession, val mapper: ObjectMapper) : MBDAOSupport(MttRulesRS::class.java, sess) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GET
    @Path("/select")
    @Transactional
//  /rs/adm/mtt/rules/select
    open fun rules(@Context req: HttpServletRequest): Response = Response.ok(StreamingOutput({ output ->
        with(JsonFactory().createGenerator(output)) {
            writeStartArray()
            selectByCriteria(createRulesQ(req), { context ->
                @Suppress("UNCHECKED_CAST")
                writeObject(context.resultObject as Map<String, Any>);
            }, "select")
            writeEndArray()
            flush()
        }
    })).type("application/json;charset=UTF-8")
            .build()

    @GET
    @Path("/select/count")
    @Produces("plain/text")
    @Transactional
//  /rs/adm/mtt/rules/select/count
    open fun rulesCount(@Context req: HttpServletRequest): Long = selectOneByCriteria(createRulesQ(req).withStatement("count"))

    private fun createRulesQ(req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        var pv: String? = req.getParameter("firstRow")
        if (pv != null) {
            val frow = Integer.valueOf(pv)
            cq.offset(frow!!)
            pv = req.getParameter("lastRow")
            if (pv != null) {
                val lrow = Integer.valueOf(pv)
                cq.limit(Math.abs(frow - lrow!!) + 1)
            }
        }

        return cq;
    };


    @GET
    @Path("/rule/{rid}")
    @Transactional
    // TODO: events?
    open fun ruleGet(@PathParam("rid") rid: Long): MttRule = selectOne(toStatementId("ruleById"), "id", rid)

//    @PUT
//    @Path("/rule")
//    open fun ruleCreate(rule: ObjectNode): ObjectNode = mapper.createObjectNode()
//
//    @POST
//    @Path("/rule/{rid}")
//    open fun ruleUpdate(@PathParam("rid") rid: Long, rule: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/rule/{rid}")
    @Transactional
    // TODO: events?
    open fun ruleDelete(@PathParam("rid") rid: Long) = delete(toStatementId("deleteRuleById"), "id", rid)

//    @PUT
//    @Path("/rule/{rid}/filter")
//    open fun filterCreate(@PathParam("rid") rid: Long, filter: ObjectNode): ObjectNode = mapper.createObjectNode()
//
//    @GET
//    @Path("/rule/{id}/filters")
//    open fun filtersList(@PathParam("rid") rid: Long): ArrayNode = mapper.createArrayNode()
//
//    @GET
//    @Path("/filter/{fid}")
//    open fun filterGet(@PathParam("fid") fid: Long): ObjectNode = mapper.createObjectNode()
//
//    @POST
//    @Path("/filter/{fid}")
//    open fun filterUpdate(@PathParam("fid") fid: Long, filter: ObjectNode): ObjectNode = mapper.createObjectNode()
//
//    @DELETE
//    @Path("/filter/{fid}")
//    open fun filterDelete(@PathParam("fid") fid: Long) = { }
//
//    @GET
//    @Path("/rule/{rid}/actions")
//    open fun actionsList(@PathParam("rid") rid: Long): ArrayNode = mapper.createArrayNode()
//
//    @PUT
//    @Path("/rule/{rid}/action")
//    open fun actionCreate(@PathParam("rid") rid: Long, action: ObjectNode): ObjectNode = mapper.createObjectNode()
//
//    @GET
//    @Path("/action/{aid}")
//    open fun actionGet(@PathParam("aid") aid: Long): ObjectNode = mapper.createObjectNode()
//
//    @POST
//    @Path("/action/{aid}")
//    open fun actionUpdate(@PathParam("aid") aid: Long, action: ObjectNode): ObjectNode = mapper.createObjectNode()
//
//    @DELETE
//    @Path("/action/{aid}")
//    open fun actionDelete(@PathParam("aid") aid: Long) = {}
//
//    @POST
//    @Path("/action/{aid}/move")
//    open fun actionMove(@PathParam("aid") aid: Long, spec: ObjectNode): ArrayNode = mapper.createArrayNode()
}