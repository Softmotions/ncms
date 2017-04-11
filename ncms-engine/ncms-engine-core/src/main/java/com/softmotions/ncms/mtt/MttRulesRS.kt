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
import org.apache.shiro.authz.annotation.RequiresRoles
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
@JvmSuppressWildcards
open class MttRulesRS
@Inject
constructor(val sess: SqlSession,
            val mapper: ObjectMapper,
            val i18n: I18n,
            val ebus: NcmsEventBus) : MBDAOSupport(MttRulesRS::class.java, sess) {

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        ebus.register(this)
    }

    @GET
    @Path("/select")
    @RequiresRoles("mtt")
    open fun rules(@Context req: HttpServletRequest): Response =
            Response.ok(StreamingOutput { output ->
                with(mapper.factory.createGenerator(output)) {
                    writeStartArray()
                    selectByCriteria(createRulesQ(req), { context ->
                        @Suppress("UNCHECKED_CAST")
                        writeObject((context.resultObject as Map<String, Any>).mapValues {
                            when {
                            // convert enabled field to boolean
                                it.key == "enabled" && it.value is Number -> it.value as Number != 0
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
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    open fun rulesCount(@Context req: HttpServletRequest): Long =
            selectOneByCriteria(createRulesQ(req), "selectRulesCount") ?: 0L

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
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleGet(@PathParam("rid") rid: Long): MttRule =
            selectOne("selectRuleById", rid) ?: throw NotFoundException()

    @PUT
    @Path("/rule/{name}")
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleCreate(@Context req: HttpServletRequest,
                        @PathParam("name") name: String): MttRule {
        synchronized(MttRule::class.java) {
            val rname = name.trim()
            if (selectOne<Long?>("selectRuleIdByName", rname) != null) {
                throw NcmsMessageException(i18n.get("ncms.mtt.rule.name.already.exists", req, rname), true)
            }
            val rule = MttRule(rname)
            insert("insertRule", rule)
            val rid = selectOne<Long?>("selectRuleIdByName", rname) ?: throw InternalServerErrorException()
            update("setRuleFirst",
                    "id", rid)

            ebus.fireOnSuccessCommit(MttRuleCreatedEvent(rid))
            return ruleGet(rid)
        }
    }

    @PUT
    @Path("/rule/rename/{rid}/{name}")
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleRename(@Context req: HttpServletRequest,
                        @PathParam("rid") rid: Long,
                        @PathParam("name") name: String): MttRule {
        synchronized(MttRule::class.java) {
            val rname = name.trim()
            if (selectOne<Long?>("selectRuleIdByName", rname) != null) {
                throw NcmsMessageException(i18n.get("ncms.mtt.rule.name.already.other", req, rname), true)
            }
            update("updateRuleName", "id", rid, "name", rname)
            ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid, hint = "rename"))
            return ruleGet(rid)
        }
    }

    @POST
    @Path("/rule/{rid}")
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleUpdate(@PathParam("rid") rid: Long, data: ObjectNode): MttRule {
        val rule = ruleGet(rid)
        with(data) {
            if (hasNonNull("flags")) rule.flags = path("flags").asLong(0)
            if (hasNonNull("description")) rule.description = path("description").asText("")
        }
        update("updateRule", rule)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid))
        return rule
    }

    @POST
    @Path("/rule/{rid}/move/up")
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleMoveUp(@PathParam("rid") rid: Long) =
            ruleMove(ruleGet(rid), false)

    @POST
    @Path("/rule/{rid}/move/down")
    @RequiresRoles("mtt")
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
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleDelete(@PathParam("rid") rid: Long): Int {
        ebus.fireOnSuccessCommit(MttRuleDeletedEvent(rid))
        return delete("deleteRuleById", rid)
    }

    @POST
    @Path("/rule/{rid}/enable")
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleEnable(@PathParam("rid") rid: Long): Int {
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid, hint = "enable"))
        return update("updateRuleEnabled", "id", rid, "enabled", true)
    }


    @POST
    @Path("/rule/{rid}/disable")
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun ruleDisable(@PathParam("rid") rid: Long): Int {
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(rid, hint = "disable"))
        return update("updateRuleEnabled", "id", rid, "enabled", false)
    }

    @GET
    @Path("/rule/{rid}/filters/select")
    @RequiresRoles("mtt")
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
                                it.key in arrayOf("enabled") && it.value is Number -> it.value as Number != 0
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
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun filtersCount(@Context req: HttpServletRequest,
                          @PathParam("rid") rid: Long): Long =
            selectOneByCriteria(createFiltersQ(rid, req), "selectFiltersCount") ?: 0L

    private fun createFiltersQ(rid: Long, req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        initCriteriaPaging(cq, req)
        cq.put("rid", rid)
        return cq
    }

    @GET
    @Path("/filter/{fid}")
    @RequiresRoles("mtt")
    @Transactional
    open fun filterGet(@PathParam("fid") fid: Long): MttRuleFilter =
            selectOne("selectFilterById", fid) ?: throw NotFoundException()

    @PUT
    @Path("/rule/{rid}/filter")
    @RequiresRoles("mtt")
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
    @RequiresRoles("mtt")
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
    @RequiresRoles("mtt")
    @Transactional
    open fun filterDelete(@PathParam("fid") fid: Long): Int {
        val filter = filterGet(fid)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(filter.ruleId))
        return delete("deleteFilterById", fid)
    }


    @GET
    @Path("/rule/{rid}/actions/select")
    @RequiresRoles("mtt")
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
                                it.key in arrayOf("enabled") && it.value is Number -> it.value as Number != 0
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
    @Produces("text/plain;charset=UTF-8")
    @RequiresRoles("mtt")
    @Transactional
    open fun actionsCount(@Context req: HttpServletRequest,
                          @PathParam("rid") rid: Long): Long =
            selectOneByCriteria(createActionsQ(rid, req), "selectActionsCount") ?: 0L

    private fun createActionsQ(rid: Long, req: HttpServletRequest): MBCriteriaQuery<out MBCriteriaQuery<*>> {
        val cq = createCriteria()
        initCriteriaPaging(cq, req)
        cq.put("rid", rid)
        cq.orderBy("ordinal")
        return cq
    }

    @GET
    @Path("/action/{aid}")
    @RequiresRoles("mtt")
    @Transactional
    open fun actionGet(@PathParam("aid") aid: Long): MttRuleAction =
            selectOne("selectActionById", aid) ?: throw NotFoundException()


    /**
     * Create new action
     */
    @PUT
    @Path("/rule/{rid}/action")
    @RequiresRoles("mtt")
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
    @RequiresRoles("mtt")
    @Transactional
    open fun compositeCreate(@PathParam("rid") rid: Long,
                             @QueryParam("groupId")
                             @DefaultValue("0") groupId: Long): MttRuleAction {
        return typedGroupCreate("composite", rid, groupId)
    }

    /**
     * Create new action group
     */
    @PUT
    @Path("/rule/{rid}/group")
    @RequiresRoles("mtt")
    @Transactional
    open fun actionGroupCreate(@PathParam("rid") rid: Long,
                               @QueryParam("groupId")
                               @DefaultValue("0") groupId: Long): MttRuleAction {
        return typedGroupCreate("group", rid, groupId)
    }

    fun typedGroupCreate(type: String, rid: Long, groupId: Long): MttRuleAction {
        val rule = ruleGet(rid)
        val action = MttRuleAction(rule.id, type)
        if (groupId > 0) {
            action.groupId = groupId
        }
        insert("insertAction", action)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(action.ruleId))
        return actionGet(action.id)
    }

    /**
     * Update rule action
     */
    @POST
    @Path("/action/{aid}")
    @RequiresRoles("mtt")
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
    @RequiresRoles("mtt")
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
    @RequiresRoles("mtt")
    @Transactional
    open fun actionDelete(@PathParam("aid") aid: Long): Int {
        val action = actionGet(aid)
        ebus.fireOnSuccessCommit(MttRuleUpdatedEvent(action.ruleId))
        return delete("deleteActionById", aid)
    }

    @POST
    @Path("/action/{aid}/move/up")
    @RequiresRoles("mtt")
    @Transactional
    open fun actionMoveUp(@PathParam("aid") aid: Long) =
            actionMove(actionGet(aid), false)

    @POST
    @Path("/action/{aid}/move/down")
    @RequiresRoles("mtt")
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
            val from = Integer.valueOf(pv)
            cq.offset(from!!)
            pv = req.getParameter("lastRow")
            if (pv != null) {
                val lrow = Integer.valueOf(pv)
                cq.limit(Math.abs(from - lrow!!) + 1)
            }
        }
    }

    /**
     * Handle page remove
     */
    @Subscribe
    fun onPageRemoved(evt: AsmRemovedEvent) {
        //todo Handle remove page
        log.info("Handle remove page")
    }
}