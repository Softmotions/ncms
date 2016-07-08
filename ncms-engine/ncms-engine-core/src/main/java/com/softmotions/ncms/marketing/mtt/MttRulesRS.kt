package com.softmotions.ncms.marketing.mtt

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

@Path("adm/mtt/rules")
@Produces("application/json;charset=UTF-8")
open class MttRulesRS
@Inject
constructor(sess: SqlSession, val mapper: ObjectMapper) : MBDAOSupport(MttRulesRS::class.java, sess) {

    //todo use as virtual list
    @GET
    @Path("/list")
    open fun rules(): ArrayNode = mapper.createArrayNode()



    @GET
    @Path("/rule/{rid}")
    open fun ruleGet(@PathParam("rid") rid: Long): ObjectNode = mapper.createObjectNode()

    @PUT
    @Path("/rule")
    open fun ruleCreate(rule: ObjectNode): ObjectNode = mapper.createObjectNode()

    @POST
    @Path("/rule/{rid}")
    open fun ruleUpdate(@PathParam("rid") rid: Long, rule: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/rule/{rid}")
    open fun ruleDelete(@PathParam("rid") rid: Long) = { }

    @PUT
    @Path("/rule/{rid}/filter")
    open fun filterCreate(@PathParam("rid") rid: Long, filter: ObjectNode): ObjectNode = mapper.createObjectNode()

    @GET
    @Path("/rule/{id}/filters")
    open fun filtersList(@PathParam("rid") rid: Long): ArrayNode = mapper.createArrayNode()

    @GET
    @Path("/filter/{fid}")
    open fun filterGet(@PathParam("fid") fid: Long): ObjectNode = mapper.createObjectNode()

    @POST
    @Path("/filter/{fid}")
    open fun filterUpdate(@PathParam("fid") fid: Long, filter: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/filter/{fid}")
    open fun filterDelete(@PathParam("fid") fid: Long) = { }

    @GET
    @Path("/rule/{rid}/actions")
    open fun actionsList(@PathParam("rid") rid: Long): ArrayNode = mapper.createArrayNode()

    @PUT
    @Path("/rule/{rid}/action")
    open fun actionCreate(@PathParam("rid") rid: Long, action: ObjectNode): ObjectNode = mapper.createObjectNode()

    @GET
    @Path("/action/{aid}")
    open fun actionGet(@PathParam("aid") aid: Long): ObjectNode = mapper.createObjectNode()

    @POST
    @Path("/action/{aid}")
    open fun actionUpdate(@PathParam("aid") aid: Long, action: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/action/{aid}")
    open fun actionDelete(@PathParam("aid") aid: Long) = {}

    @POST
    @Path("/action/{aid}/move")
    open fun actionMove(@PathParam("aid") aid: Long, spec: ObjectNode): ArrayNode = mapper.createArrayNode()
}