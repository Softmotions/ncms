package com.softmotions.ncms.marketing.rules

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.Inject
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.ibatis.session.SqlSession
import javax.ws.rs.*

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */

@Path("adm/marketing/tools/rules")
@Produces("application/json;charset=UTF-8")
class RulesRS
@Inject
constructor(sess: SqlSession, val mapper: ObjectMapper) : MBDAOSupport (RulesRS::class.java, sess) {

    @GET
    @Path("/list")
    fun rules(): ArrayNode = mapper.createArrayNode()

    @GET
    @Path("/rule/{rid}")
    fun ruleGet(@PathParam("rid") rid: Long): ObjectNode = mapper.createObjectNode()

    @PUT
    @Path("/rule")
    fun ruleCreate(rule: ObjectNode): ObjectNode = mapper.createObjectNode()

    @POST
    @Path("/rule/{rid}")
    fun ruleUpdate(@PathParam("rid") rid: Long, rule: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/rule/{rid}")
    fun ruleDelete(@PathParam("rid") rid: Long) = { }

    @PUT
    @Path("/rule/{rid}/filter")
    fun filterCreate(@PathParam("rid") rid: Long, filter: ObjectNode): ObjectNode = mapper.createObjectNode()

    @GET
    @Path("/rule/{id}/filters")
    fun filtersList(@PathParam("rid") rid: Long): ArrayNode = mapper.createArrayNode()

    @GET
    @Path("/filter/{fid}")
    fun filterGet(@PathParam("fid") fid: Long): ObjectNode = mapper.createObjectNode()

    @POST
    @Path("/filter/{fid}")
    fun filterUpdate(@PathParam("fid") fid: Long, filter: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/filter/{fid}")
    fun filterDelete(@PathParam("fid") fid: Long) = { }

    @GET
    @Path("/rule/{rid}/actions")
    fun actionsList(@PathParam("rid") rid: Long): ArrayNode = mapper.createArrayNode()

    @PUT
    @Path("/rule/{rid}/action")
    fun actionCreate(@PathParam("rid") rid: Long, action: ObjectNode): ObjectNode = mapper.createObjectNode()

    @GET
    @Path("/action/{aid}")
    fun actionGet(@PathParam("aid") aid: Long): ObjectNode = mapper.createObjectNode()

    @POST
    @Path("/action/{aid}")
    fun actionUpdate(@PathParam("aid") aid: Long, action: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/action/{aid}")
    fun actionDelete(@PathParam("aid") aid: Long) = {}

    @POST
    @Path("/action/{aid}/move")
    fun actionMove(@PathParam("aid") aid: Long, spec: ObjectNode): ArrayNode = mapper.createArrayNode()
}