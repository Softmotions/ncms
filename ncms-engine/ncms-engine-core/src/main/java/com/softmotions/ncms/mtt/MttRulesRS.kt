package com.softmotions.ncms.mtt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.softmotions.ncms.asm.events.AsmRemovedEvent
import com.softmotions.ncms.events.NcmsEventBus
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
            val messages: I18n,
            val ebus: NcmsEventBus) : MBDAOSupport(MttRulesRS::class.java, sess) {

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
                        writeObject((context.resultObject as Map<String, Any>).mapValues {
                            when {
                            // convert enabled field to boolean
                                it.key in arrayOf("enabled") -> it.value as Number != 0
                                else -> it.value
                            }
                        })
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
        return cq
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
            val rule = MttRule(rname)
            insert("insertRule", rule)
            val rid = selectOne<Long?>("selectRuleIdByName", rname) ?: throw InternalServerErrorException()
            ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
            return ruleGet(rid)
        }
    }

    @PUT
    @Path("/rule/rename/{rid}/{name}")
    @Transactional
    open fun ruleRename(@Context req: HttpServletRequest,
                        @PathParam("rid") rid: Long,
                        @PathParam("name") name: String): MttRule {
        synchronized(MttRule::class) {
            val rname = name.trim()
            if (selectOne<Long?>("selectRuleIdByName", rname) != null) {
                throw NcmsMessageException(messages.get("ncms.mtt.rule.name.already.other", req, rname), true)
            }
            update("updateRuleName", "id", rid, "name", rname)
            ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
            return ruleGet(rid)
        }
    }

    @POST
    @Path("/rule/{rid}")
    @Transactional
    open fun ruleUpdate(@PathParam("rid") rid: Long, rn: ObjectNode): MttRule {
        val rule = ruleGet(rid)
        with(rn) {
            if (hasNonNull("flags")) rule.flags = path("flags").asLong(0)
            if (hasNonNull("description")) rule.description = path("description").asText()
        }
        update("updateRule", rule)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
        return ruleGet(rid)
    }

    @POST
    @Path("/rule/{rid}/move/up")
    @Transactional
    open fun ruleMoveUp(@PathParam("rid") rid: Long) =
            ruleMove(ruleGet(rid), false)

    @POST
    @Path("/rule/{rid}/move/down")
    @Transactional
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
        update("exchangeRuleOrdinal",
                "ordinal1", sordinal,
                "ordinal2", rule.ordinal)

        ebus.fireOnSuccessCommit(MttRuleReorderedEvent(sordinal, rule.ordinal))
    }

    @DELETE
    @Path("/rule/{rid}")
    @Transactional
    open fun ruleDelete(@PathParam("rid") rid: Long): Int {
        ebus.fireOnSuccessCommit(MttRuleDeletedEvent(rid))
        return delete("deleteRuleById", rid)
    }

    @POST
    @Path("/rule/{rid}/enable")
    @Transactional
    open fun ruleEnable(@PathParam("rid") rid: Long): Int {
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
        return update("updateRuleEnabled", "id", rid, "enabled", true)
    }


    @POST
    @Path("/rule/{rid}/disable")
    @Transactional
    open fun ruleDisable(@PathParam("rid") rid: Long): Int {
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
        return update("updateRuleEnabled", "id", rid, "enabled", false)
    }

    @GET
    @Path("/rule/{rid}/filters/select")
    @Transactional
    open fun filters(@Context req: HttpServletRequest, @PathParam("rid") rid: Long): Response =
            Response.ok(StreamingOutput { output ->
                with(mapper.factory.createGenerator(output)) {
                    writeStartArray()
                    selectByCriteria(createFiltersQ(rid, req), { context ->
                        @Suppress("UNCHECKED_CAST")
                        writeObject((context.resultObject as Map<String, Any>).mapValues {
                            when {
                            // convert enabled field to boolean
                                it.key in arrayOf("enabled") -> it.value as Number != 0
                                else -> it.value
                            }
                        })
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
    @Path("/rule/{rid}/filter")
    @Transactional
    open fun filterCreate(@PathParam("rid") rid: Long, fn: ObjectNode): MttRuleFilter {
        val rule = ruleGet(rid)
        val type = fn.path("type").asText(null) ?: throw BadRequestException()
        val filter = MttRuleFilter(rule.id, type.trim())
        with(filter) {
            spec = fn.path("spec").asText(null)
            description = fn.path("description").asText(null)
            enabled = fn.path("enabled").asBoolean(true)
            insert("insertFilter", filter)
            ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
            return filterGet(id)
        }
    }

    @POST
    @Path("/filter/{fid}")
    @Transactional
    open fun filterUpdate(@PathParam("fid") fid: Long, fn: ObjectNode): MttRuleFilter {
        val filter = filterGet(fid)
        if (fn.hasNonNull("type")) filter.type = fn["type"].asText()
        if (fn.hasNonNull("description")) filter.description = fn["description"].asText()
        if (fn.hasNonNull("enabled")) filter.enabled = fn["enabled"].asBoolean()
        if (fn.hasNonNull("spec")) filter.spec = fn["spec"].asText()
        update("updateFilter", filter)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(filter.ruleId))
        return filterGet(filter.id)
    }

    @DELETE
    @Path("/filter/{fid}")
    @Transactional
    open fun filterDelete(@PathParam("fid") fid: Long): Int {
        val filter = filterGet(fid)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(filter.ruleId))
        return delete("deleteFilterById", fid)
    }


    @GET
    @Path("/rule/{rid}/actions/select")
    @Transactional
    open fun actions(@Context req: HttpServletRequest, @PathParam("rid") rid: Long): Response =
            Response.ok(StreamingOutput { output ->
                with(mapper.factory.createGenerator(output)) {
                    writeStartArray()
                    selectByCriteria(createActionsQ(rid, req), { context ->
                        @Suppress("UNCHECKED_CAST")
                        writeObject((context.resultObject as Map<String, Any>).mapValues {
                            when {
                            // convert enabled field to boolean
                                it.key in arrayOf("enabled") -> it.value as Number != 0
                                else -> it.value
                            }
                        })
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


    /**
     * Create new action
     */
    @PUT
    @Path("/rule/{rid}/action")
    @Transactional
    open fun actionCreate(@PathParam("rid") rid: Long,
                          an: ObjectNode): MttRuleAction {
        val rule = ruleGet(rid)
        val type = an.path("type").asText(null) ?: throw BadRequestException()
        val action = MttRuleAction(rule.id, type.trim())
        with(action) {
            spec = an.path("spec").asText(null)
            description = an.path("description").asText(null)
            enabled = an.path("enabled").asBoolean(true)
            groupId = if (an.path("groupId").isNumber()) an.path("groupId").longValue() else null
            insert("insertAction", action)
            ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
            return actionGet(id)
        }
    }

    /**
     * Create new composite action group
     */
    @PUT
    @Path("/rule/{rid}/composite")
    @Transactional
    open fun compositeCreate(@PathParam("rid") rid: Long,
                             @QueryParam("groupId")
                             @DefaultValue("0") groupId: Long): MttRuleAction {

        log.info("compositeCreate rid=${rid} groupId=${groupId}")
        val rule = ruleGet(rid)
        val action = MttRuleAction(rule.id, "composite")
        if (groupId > 0) {
            action.groupId = groupId
        }
        insert("insertAction", action)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(action.ruleId))
        return actionGet(action.id)
    }

    /**
     * Create new action group
     */
    @PUT
    @Path("/rule/{rid}/group/{name}")
    @Transactional
    open fun actionGroupCreate(@PathParam("rid") rid: Long,
                               @PathParam("name") name: String): MttRuleAction {
        val rule = ruleGet(rid)
        val action = MttRuleAction(rule.id, "group")
        with(action) {
            description = name
            insert("insertAction", action)
            ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(ruleId))
            return actionGet(id)
        }
    }

    /**
     * Update action group
     */
    @POST
    @Path("/group/{id}/{name}")
    @Transactional
    open fun actionGroupUpdate(@PathParam("id") id: Long,
                               @PathParam("name") name: String): MttRuleAction {
        val action = actionGet(id)
        with(action) {
            type = "group"
            description = name
            update("updateAction", action)
            ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(ruleId))
            return actionGet(id)
        }
    }

    /**
     * Update rule action
     */
    @POST
    @Path("/action/{aid}")
    @Transactional
    open fun actionUpdate(@PathParam("aid") aid: Long, an: ObjectNode): MttRuleAction {
        val action = actionGet(aid)
        if (an.hasNonNull("type")) action.type = an["type"].asText()
        if (an.hasNonNull("description")) action.description = an["description"].asText()
        if (an.hasNonNull("spec")) action.spec = an["spec"].asText()
        if (an.hasNonNull("enabled")) action.enabled = an["enabled"].asBoolean()
        update("updateAction", action)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(action.ruleId))
        return actionGet(action.id)
    }

    /**
     * Update action group width
     */
    @POST
    @Path("/weight/{id}/{weight}")
    @Transactional
    open fun actionWeghtUpdate(@PathParam("id") id: Long,
                               @PathParam("weight") weight: Int): Unit {
        val action = actionGet(id)
        update("updateActionWidth",
                "id", id,
                "weight", weight)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(action.ruleId))
    }

    @DELETE
    @Path("/action/{aid}")
    @Transactional
    open fun actionDelete(@PathParam("aid") aid: Long): Int {
        val action = actionGet(aid)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(action.ruleId))
        return delete("deleteActionById", aid)
    }

    @POST
    @Path("/action/{aid}/move/up")
    @Transactional
    open fun actionMoveUp(@PathParam("aid") aid: Long) =
            actionMove(actionGet(aid), false)

    @POST
    @Path("/action/{aid}/move/down")
    @Transactional
    open fun actionMoveDown(@PathParam("aid") aid: Long) =
            actionMove(actionGet(aid), true)

    private fun actionMove(action: MttRuleAction, direction: Boolean) {
        val sordinal = when {
            direction -> selectOne<Long?>("selectNextRuleAction",
                    "rid", action.ruleId,
                    "ordinal", action.ordinal,
                    "groupId", action.groupId)
            else -> selectOne<Long?>("selectPreviousRuleAction",
                    "rid", action.ruleId,
                    "ordinal", action.ordinal,
                    "groupId", action.groupId)
        }
        sordinal ?: return
        update("exchangeActionOrdinal",
                "ordinal1", sordinal,
                "ordinal2", action.ordinal)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(action.ruleId))
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

    /**
     * Handle page remove
     */
    @Subscribe
    fun pageRemoved(evt: AsmRemovedEvent) {
        log.info("Handle remove page")
    }
}