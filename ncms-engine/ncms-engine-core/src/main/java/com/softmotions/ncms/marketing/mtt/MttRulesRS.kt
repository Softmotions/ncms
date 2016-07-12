package com.softmotions.ncms.marketing.mtt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
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
    open fun rules(@Context req: HttpServletRequest): Response =
            Response.ok(StreamingOutput { output ->
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
    open fun rulesCount(@Context req: HttpServletRequest): Long =
            selectOneByCriteria(createRulesQ(req), "selectRulesCount")

    private fun createRulesQ(req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        val pv: String?

        pv = req.getParameter("stext")
        if (!StringUtils.isBlank(pv)) {
            cq.put("name", "${pv.trim().toLowerCase()}%")
        }

        initCriteriaPaging(cq, req)
        cq.orderBy("ordinal")
        return cq;
    }

    @GET
    @Path("/rule/{rid}")
    @Transactional
    open fun ruleGet(@PathParam("rid") rid: Long): MttRule =
            selectOne("selectRuleById", rid) ?: throw NotFoundException()

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
                        @PathParam("name") name: String): MttRule =
            synchronized(MttRule::class) {
                val rname = name.trim();
                if (selectOne<Long?>("selectRuleIdByName", rname) != null) {
                    throw NcmsMessageException(messages.get("ncms.mtt.rule.name.already.other", req, rname), true)
                }

                update("updateRuleName", "id", rid, "name", rname);

                return ruleGet(rid);
            }

    @POST
    @Path("/rule/{rid}/flags/{flags}")
    open fun ruleUpdateFlags(@PathParam("rid") rid: Long, @PathParam("flags") flags: Long): MttRule {
        update("updateRuleFlags", "id", rid, "flags", flags)
        return ruleGet(rid)
    }

    @POST
    @Path("/rule/{rid}/move/up")
    open fun ruleMoveUp(@PathParam("rid") rid: Long) =
            ruleMove(ruleGet(rid), false)

    @POST
    @Path("/rule/{rid}/move/down")
    open fun ruleMoveDown(@PathParam("rid") rid: Long) =
            ruleMove(ruleGet(rid), true)

    /**
     * @param direction true - move down (increase ordinal), false - move up (decrease ordinal)
     */
    private fun ruleMove(rule: MttRule, direction: Boolean) {
        val sordinal = when {
            direction -> selectOne<Long?>("selectNextRule", rule.ordinal)
            else -> selectOne<Long?>("selectPreviousRule", rule.ordinal)
        }
        sordinal ?: return
        update("updateRuleOrdinal", "ordinal", sordinal, "newOrdinal", rule.ordinal)
        update("updateRuleOrdinal", "id", rule.id, "newOrdinal", sordinal)
    }

    @DELETE
    @Path("/rule/{rid}")
    @Transactional
    // TODO: events?
    open fun ruleDelete(@PathParam("rid") rid: Long) =
            delete("deleteRuleById", rid)


    @POST
    @Path("/rule/{rid}/enable")
    open fun ruleEnable(@PathParam("rid") rid: Long) =
            update("updateRuleEnabled", "id", rid, "enabled", true)

    @POST
    @Path("/rule/{rid}/disable")
    open fun ruleDisable(@PathParam("rid") rid: Long) =
            update("updateRuleEnabled", "id", rid, "enabled", false)

    // TODO: rule enabling/disabling

    // TODO: rule description edit

    @GET
    @Path("/rule/{rid}/filters/select")
    @Transactional
    open fun filters(@Context req: HttpServletRequest, @PathParam("rid") rid: Long): Response =
            Response.ok(StreamingOutput { output ->
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
    open fun filtersCount(@Context req: HttpServletRequest, @PathParam("rid") rid: Long): Long =
            selectOneByCriteria(createFiltersQ(rid, req), "selectFiltersCount")

    private fun createFiltersQ(rid: Long, req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        initCriteriaPaging(cq, req)
        cq.put("rid", rid)
        return cq
    }

    @GET
    @Path("/filter/{fid}")
    @Transactional
    open fun filterGet(@PathParam("fid") fid: Long): MttRuleFilter =
            selectOne("selectFilterById", fid) ?: throw NotFoundException()

    @PUT
    @Path("/rule/{rid}/filter/{type}")
    @Transactional
    open fun filterCreate(@PathParam("rid") rid: Long, @PathParam("type") type: String): MttRuleFilter {
        val rule = ruleGet(rid)
        if (StringUtils.isBlank(type)) throw BadRequestException()
        val filter = MttRuleFilter(type = type.trim(), ruleId = rule.id)
        insert("insertFilter", filter)
        return filterGet(filter.id)
    }

    @POST
    @Path("/filter/{fid}")
    @Transactional
    open fun filterUpdate(@PathParam("fid") fid: Long, filterNode: ObjectNode): MttRuleFilter {
        val filter = filterGet(fid)

        if (filterNode.hasNonNull("description")) filter.description = filterNode["description"].asText()
        if (filterNode.hasNonNull("spec")) filter.spec = filterNode["spec"].asText()

        update("updateFilter", filter)

        return filterGet(filter.id)
    }

    @DELETE
    @Path("/filter/{fid}")
    @Transactional
    open fun filterDelete(@PathParam("fid") fid: Long) =
            delete("deleteFilterById", fid)

    @GET
    @Path("/rule/{rid}/actions/select")
    @Transactional
    open fun actions(@Context req: HttpServletRequest, @PathParam("rid") rid: Long): Response =
            Response.ok(StreamingOutput { output ->
                with(mapper.factory.createGenerator(output)) {
                    writeStartArray()
                    selectByCriteria(createActionsQ(rid, req), { context ->
                        @Suppress("UNCHECKED_CAST")
                        writeObject(context.resultObject as Map<String, Any>)
                    }, "selectActions")
                    writeEndArray()
                    flush()
                }
            })
                    .type("application/json;charset=UTF-8")
                    .build()

    @GET
    @Path("/rule/{rid}/actions/select/count")
    @Produces("text/plain")
    @Transactional
    open fun actionsCount(@Context req: HttpServletRequest, @PathParam("rid") rid: Long): Long =
            selectOneByCriteria(createActionsQ(rid, req), "selectActionsCount")

    private fun createActionsQ(rid: Long, req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        initCriteriaPaging(cq, req)
        cq.put("rid", rid)
        cq.orderBy("ordinal")
        return cq
    }

    @GET
    @Path("/action/{aid}")
    @Transactional
    open fun actionGet(@PathParam("aid") aid: Long): MttRuleAction =
            selectOne("selectActionById", aid) ?: throw NotFoundException()

    @PUT
    @Path("/rule/{rid}/action/{type}")
    @Transactional
    open fun actionCreate(@PathParam("rid") rid: Long, @PathParam("type") type: String): MttRuleAction {
        val rule = ruleGet(rid)
        if (StringUtils.isBlank(type)) throw BadRequestException()
        val action = MttRuleAction(type = type.trim(), ruleId = rule.id)
        insert("insertAction", action)
        return actionGet(action.id)
    }

    @POST
    @Path("/action/{aid}")
    open fun actionUpdate(@PathParam("aid") aid: Long, actionNode: ObjectNode): MttRuleAction {
        val action = actionGet(aid)

        if (actionNode.hasNonNull("description")) action.description = actionNode.path("description").asText()
        if (actionNode.hasNonNull("spec")) action.spec = actionNode.path("spec").asText()

        return actionGet(action.id)
    }

    @DELETE
    @Path("/action/{aid}")
    open fun actionDelete(@PathParam("aid") aid: Long) =
            delete("deleteActionById", aid)

    @POST
    @Path("/action/{aid}/move/up")
    open fun actionMoveUp(@PathParam("aid") aid: Long) =
            actionMove(actionGet(aid), false)

    @POST
    @Path("/action/{aid}/move/down")
    open fun actionMoveDown(@PathParam("aid") aid: Long) =
            actionMove(actionGet(aid), true)

    private fun actionMove(action: MttRuleAction, direction: Boolean) {
        val sordinal = when {
            direction -> selectOne<Long?>("selectNextRuleAction", "rid", action.ruleId, "ordinal", action.ordinal)
            else -> selectOne<Long?>("selectPreviousRuleAction", "rid", action.ruleId, "ordinal", action.ordinal)
        }

        sordinal ?: return
        update("updateActionOrdinal", "ordinal", sordinal, "newOrdinal", action.ordinal)
        update("updateActionOrdinal", "id", action.id, "newOrdinal", sordinal)
    }

    private fun initCriteriaPaging(cq: MBCriteriaQuery<MBCriteriaQuery<*>>, req: HttpServletRequest) {
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
    }
}