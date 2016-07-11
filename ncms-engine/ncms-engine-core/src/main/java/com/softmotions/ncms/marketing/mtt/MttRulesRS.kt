package com.softmotions.ncms.marketing.mtt

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.softmotions.ncms.jaxrs.NcmsMessageException
import com.softmotions.weboot.i18n.I18n
import com.softmotions.weboot.mb.MBCriteriaQuery
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.commons.lang3.StringUtils
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
constructor(val sess: SqlSession,
            val mapper: ObjectMapper,
            val messages: I18n) : MBDAOSupport(MttRulesRS::class.java, sess) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GET
    @Path("/select")
    @Transactional
    open fun rules(@Context req: HttpServletRequest): Response = Response
            .ok(StreamingOutput { output ->
                with(mapper.factory.createGenerator(output)) {
                    writeStartArray()
                    selectByCriteria(createRulesQ(req), { context ->
                        @Suppress("UNCHECKED_CAST")
                        writeObject(context.resultObject as Map<String, Any>);
                    }, "selectRules")
                    writeEndArray()
                    flush()
                }
            })
            .type("application/json;charset=UTF-8")
            .build()

    @GET
    @Path("/select/count")
    @Produces("text/plain")
    @Transactional
    open fun rulesCount(@Context req: HttpServletRequest): Int = selectOneByCriteria(createRulesQ(req).withStatement("selectRulesCount"))

    private fun createRulesQ(req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        var pv: String?

        pv = req.getParameter("stext")
        if (!StringUtils.isBlank(pv)) {
            cq.put("name", "${pv.trim().toLowerCase()}%")
        }

        pv = req.getParameter("firstRow")
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
    }

    @GET
    @Path("/rule/{rid}")
    @Transactional
    open fun ruleGet(@PathParam("rid") rid: Long): MttRule = selectOne("selectRuleById", rid)

    @PUT
    @Path("/rule/{name}")
    @Transactional
    // TODO: events?
    open fun ruleCreate(@Context req: HttpServletRequest,
                        @PathParam("name") name: String): MttRule {
        synchronized(MttRule::class) {
            val rname = name.trim()
            if (selectOne<Long?>("selectRuleIdByName", rname) != null) {
                throw NcmsMessageException(messages.get("ncms.mtt.rule.name.already.exists", req, rname), true)
            }
            val rule = MttRule(name = rname)

            insert("insertRule", rule)
            val rid = selectOne<Long?>("selectRuleIdByName", rname) ?: throw InternalServerErrorException()
            return ruleGet(rid);
        }
    }

    @PUT
    @Path("/rule/rename/{rid}/{name}")
    @Transactional
    open fun ruleRename(@Context req: HttpServletRequest,
                        @PathParam("rid") rid: Long,
                        @PathParam("name") name: String): MttRule = synchronized(MttRule::class) {
        val rname = name.trim();
        if (selectOne<Long?>("selectRuleIdByName", rname) != null) {
            throw NcmsMessageException(messages.get("ncms.mtt.rule.name.already.other", req, rname), true)
        }

        update("updateRuleName", "id", rid, "name", rname);

        return ruleGet(rid);
    }

//    @POST
//    @Path("/rule/{rid}")
//    open fun ruleUpdate(@PathParam("rid") rid: Long, rule: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/rule/{rid}")
    @Transactional
    // TODO: events?
    open fun ruleDelete(@PathParam("rid") rid: Long) = delete("deleteRuleById", rid)

    @GET
    @Path("/rule/{rid}/filters/select")
    @Transactional
    open fun filters(@Context req: HttpServletRequest,
                     @PathParam("rid") rid: Long): Response = Response
            .ok(StreamingOutput { output ->
                with(mapper.factory.createGenerator(output)) {
                    writeStartArray()
                    selectByCriteria(createFiltersQ(rid, req), { context ->
                        @Suppress("UNCHECKED_CAST")
                        writeObject(context.resultObject as Map<String, Any>)
                    }, "selectFilters")
                    writeEndArray()
                    flush()
                }
            })
            .type("application/json;charset=UTF-8")
            .build()

    @GET
    @Path("/rule/{rid}/filters/select/count")
    @Produces("text/plain")
    @Transactional
    open fun filtersCount(@Context req: HttpServletRequest,
                          @PathParam("rid") rid: Long): Long = selectOneByCriteria(createFiltersQ(rid, req).withStatement("selectFiltersCount"))

    private fun createFiltersQ(rid: Long, req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
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

        cq.put("rid", rid)

        return cq
    }


//    @PUT
//    @Path("/rule/{rid}/filter")
//    open fun filterCreate(@PathParam("rid") rid: Long, filter: ObjectNode): MttRuleFilter = MttRuleFilter()

    @GET
    @Path("/filter/{fid}")
    open fun filterGet(@PathParam("fid") fid: Long): MttRuleFilter = selectOne("selectFilterById", fid)

//    @POST
//    @Path("/filter/{fid}")
//    open fun filterUpdate(@PathParam("fid") fid: Long, filter: ObjectNode): ObjectNode = mapper.createObjectNode()

    @DELETE
    @Path("/filter/{fid}")
    open fun filterDelete(@PathParam("fid") fid: Long) = delete("deleteFilterById", fid)

//    @GET
//    @Path("/rule/{rid}/actions")
//    open fun actionsList(@PathParam("rid") rid: Long): ArrayNode = mapper.createArrayNode()

//    @PUT
//    @Path("/rule/{rid}/action")
//    open fun actionCreate(@PathParam("rid") rid: Long, action: ObjectNode): ObjectNode = mapper.createObjectNode()

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