package com.softmotions.ncms.mtt.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.NcmsEnvironment
import com.softmotions.ncms.events.NcmsEventBus
import com.softmotions.ncms.mtt.*
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.commons.lang3.StringUtils
import org.apache.ibatis.session.SqlSession
import org.mybatis.guice.transactional.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.annotation.concurrent.GuardedBy
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Http traffic MTT filter.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@JvmSuppressWildcards
open class MttHttpFilter
@Inject
constructor(val ebus: NcmsEventBus,
            val mapper: ObjectMapper,
            val env: NcmsEnvironment,
            filters: Set<MttFilterHandler>,
            actions: Set<MttActionHandler>,
            sess: SqlSession) : MBDAOSupport(MttHttpFilter::class.java, sess), Filter {

    companion object {

        @JvmField
        val MTT_RIDS_KEY = "MTTR"

        private val log: Logger = LoggerFactory.getLogger(MttHttpFilter::class.java);
    }

    /**
     * Dynamic resources URI patterns
     */
    private val DEFAULT_DYN_RE = Regex("^((.*\\.(jsp|htm|html|httl|vm|ftl))|([^\\.]+))$", RegexOption.IGNORE_CASE)

    private val lock = ReentrantReadWriteLock()

    private val filterHandlers: Map<String, MttFilterHandler> = HashMap<String, MttFilterHandler>().apply {
        filters.forEach {
            log.info("Register MTT filter: '${it.type}' class: ${it.javaClass.name}")
            put(it.type, it)
        }
    }

    private val actionHandlers: Map<String, MttActionHandler> = HashMap<String, MttActionHandler>().apply {
        actions.forEach {
            log.info("Register MTT action: '${it.type}' class: ${it.javaClass.name}")
            put(it.type, it)
        }
    }

    @GuardedBy("lock")
    private val id2slots = LinkedHashMap<Long, RuleSlot>()

    override fun init(cfg: FilterConfig?) {
        ebus.register(this)
        loadRules()
    }

    override fun doFilter(req: ServletRequest?, resp: ServletResponse?, chain: FilterChain?) {
        resp as HttpServletResponse
        val rmc = MttRequestModificationContext(req as HttpServletRequest)
        if (!processRules(rmc, resp)) {
            chain!!.doFilter(rmc.applyModifications(), resp);
        }
    }

    override fun destroy() {
        ebus.unregister(this)
    }

    /**
     * Returns `true` if request completely handled by some rule action
     */
    private fun processRules(rmc: MttRequestModificationContext, resp: HttpServletResponse): Boolean {
        var ret = false
        val req = rmc.req
        if (req.method != "GET" || req.getAttribute(MTT_RIDS_KEY) != null) {
            return false
        }
        val uri = req.requestURI
        // If admin resource
        // OR non dynamic resource
        // OR /rs/media
        // OR /rs/adm
        if (uri.startsWith(env.ncmsAdminRoot)
                || uri.startsWith(env.appRoot + "/rs/adm")
                || uri.startsWith(env.appRoot + "/rs/media")
                || !DEFAULT_DYN_RE.matches(uri)) {
            return false;
        }

        val rids = LinkedHashSet<Long>()
        req.setAttribute(MTT_RIDS_KEY, rids)
        // URI=/ URL=http://vk.smsfinance.ru:9191/ QS=test=foo
        // URI=/rs/media/fileid/286 URL=http://vk.smsfinance.ru:9191/rs/media/fileid/286 QS=w=300&h=300
        // URI=/rs/adm/ws/state URL=http://localhost:9191/rs/adm/ws/state QS=nocache=1469768085685
        lock.read {
            for (rs in id2slots.values) {
                rids.add(rs.id)
                if (rs.runRule(rmc, resp)) {
                    ret = true
                    break
                }
            }
        }
        return ret
    }

    @Transactional
    internal open fun loadRules() {
        val rules = select<MttRule>("selectMttRules")
        rules.forEach {
            activateRule(it)
        }
    }

    @Subscribe
    fun onRuleUpdated(event: MttRuleUpdatedEvent) {
        if (log.isDebugEnabled) {
            log.debug("Rule updated=${event}")
        }
        if (event.hint == "rename") {
            if (log.isDebugEnabled) {
                log.debug("Rule renamed, activation will be skipped")
            }
            return
        }
        activateRule(event.ruleId)
    }

    @Subscribe
    fun onRuleDeleted(event: MttRuleDeletedEvent) {
        if (log.isDebugEnabled) {
            log.debug("Rule deleted=${event}")
        }
        lock.write {
            id2slots.remove(event.ruleId)
        }
    }

    @Subscribe
    fun onRuleCreated(event: MttRuleCreatedEvent) {
        if (log.isDebugEnabled) {
            log.debug("Rule created=${event}")
        }
        lock.write {
            activateRule(event.ruleId)
            val slots = id2slots.values.toTypedArray()
            val snew = slots.find { it.rule.id == event.ruleId }
            if (snew == null) {
                return
            }
            slots.forEach {
                it.rule.ordinal++
            }
            snew.rule.ordinal = 1
            slots.sort()
            id2slots.clear()
            slots.forEach {
                id2slots[it.id] = it
            }
        }
    }


    @Subscribe
    fun onRuleReordered(event: MttRuleReorderedEvent) {
        if (log.isDebugEnabled) {
            log.debug("Rule reordered=${event}")
        }
        lock.write {
            val slots = id2slots.values.toTypedArray()
            val s1 = slots.find { it.rule.ordinal == event.ordinal1 }
            val s2 = slots.find { it.rule.ordinal == event.ordinal2 }
            if (s1 == null || s2 == null) {
                return;
            }
            val tmp = s1.rule.ordinal
            s1.rule.ordinal = s2.rule.ordinal
            s2.rule.ordinal = tmp
            slots.sort() // resort rules
            id2slots.clear()
            slots.forEach {
                id2slots[it.id] = it
            }
        }
    }

    private fun activateRule(ruleId: Long) {
        val rule = selectOne<MttRule?>("selectMttRules", "id", ruleId) ?: return
        activateRule(rule)
    }

    private fun activateRule(rule: MttRule) {
        lock.write {
            val nslot = RuleSlot(rule)
            val slot = id2slots[rule.id]
            if (slot == null) {
                id2slots[rule.id] = nslot
            } else {
                slot.rule = rule
            }
        }
    }

    inner class RuleSlot : Comparable<RuleSlot> {

        private lateinit var _rule: MttRule

        override fun compareTo(other: RuleSlot): Int {
            return _rule.ordinal.compareTo(other._rule.ordinal)
        }

        val id: Long
            get() = _rule.id

        var rule: MttRule
            get() = _rule
            set(value) {
                initRule(value)
            }

        // List of filter,context
        internal val filters = ArrayList<MttFilterHandlerContextImpl>()

        // List of action,context
        internal val actions = ArrayList<MttActionHandlerContextImpl>()

        constructor(rule: MttRule) {
            this.rule = rule
        }

        @GuardedBy("lock") //write
        private fun initRule(rule: MttRule) {
            // Filters
            filters.clear()
            filters.addAll(rule.filters.filter {
                filterHandlers[it.type] != null
            }.sortedBy {
                when (it.type) {
                    "vhosts" -> 1
                    "useragent" -> 2
                    "params" -> 10
                    "headers" -> 11
                    "cookies" -> 12
                    "resource" -> 13
                    "page" -> 20
                    else -> 100
                }
            }.map {
                val spec = if (!StringUtils.isBlank(it.spec)) mapper.readTree(it.spec) else mapper.createObjectNode()
                MttFilterHandlerContextImpl(it, rule, spec as ObjectNode, filterHandlers[it.type]!!)
            })

            // Actions
            actions.clear()
            actions.addAll(rule.actions.filter {
                actionHandlers[it.type] != null
            }.map {
                val spec = if (!StringUtils.isBlank(it.spec)) mapper.readTree(it.spec) else mapper.createObjectNode()
                MttActionHandlerContextImpl(it, rule, spec as ObjectNode, actionHandlers[it.type]!!, this)
            })
            this._rule = rule
        }

        /**
         * @return `True` if the response handled by this rule
         */
        @GuardedBy("lock") //read
        fun runRule(rmc: MttRequestModificationContext, resp: HttpServletResponse): Boolean {
            if (!rule.enabled) {
                return false
            }
            val req = rmc.req
            if (log.isDebugEnabled) {
                log.debug("Run rule=${rule.name} id=${rule.id}")
            }
            // Run filters
            val passed = filters.all {
                try {
                    val ret = it.handler.matched(it, req)
                    if (log.isDebugEnabled) {
                        log.debug("Run filter=${it.spec} ret=${ret}")
                    }
                    return@all ret
                } catch(e: Throwable) {
                    log.error("Rule filter error ${it.javaClass}", e)
                    return@all false
                }
            }
            if (!passed) {
                if (log.isDebugEnabled) {
                    log.debug("Rule=${rule.name} NOT matched resource=${req.requestURL} qs=${req.queryString}")
                }
                return false
            }
            if (log.isDebugEnabled) {
                log.debug("Rule=${rule.name} matched resource=${req.requestURL}")
            }
            // All filters matched, now run actions
            var ret = false
            for (a in actions) {
                if (!a.action.enabled || a.action.groupId != null) {
                    // only enabled actions can me runned
                    // this action will be runned by the special MttGroupAction
                    //if (log.isDebugEnabled) {
                    //    log.debug(
                    //            "Rule action skipped ${a.javaClass.name} " +
                    //                    "enabled=${a.action.enabled} " +
                    //                   "groupId=${a.action.groupId}")
                    //}
                    continue
                }
                ret = a.execute(rmc, resp)
                if (ret) {
                    break
                }
            }
            return ret

        }
    }

    internal class MttFilterHandlerContextImpl(override val filter: MttRuleFilter,
                                               override val rule: MttRule,
                                               override val spec: ObjectNode,
                                               internal val handler: MttFilterHandler)
    : ConcurrentHashMap<String, Any?>(), MttFilterHandlerContext {

    }


    internal inner class MttActionHandlerContextImpl(override val action: MttRuleAction,
                                                     override val rule: MttRule,
                                                     override val spec: ObjectNode,
                                                     internal val handler: MttActionHandler,
                                                     internal val slot: RuleSlot)
    : ConcurrentHashMap<String, Any?>(), MttActionHandlerContext {

        override fun findGroupActions(): List<MttActionHandlerContext> {
            return slot.actions.filter {
                it.action.enabled && it.action.groupId == action.id
            }
        }

        override fun execute(rmc: MttRequestModificationContext,
                             resp: HttpServletResponse): Boolean {
            if (!action.enabled) {
                return false
            }
            var ret = false;
            try {
                ret = handler.execute(this, rmc, resp)
            } catch(e: Throwable) {
                log.error("Rule action error ${handler.javaClass}", e)
            }
            if (log.isDebugEnabled) {
                log.debug("Rule action: ${handler.javaClass} executed, ret=${ret}")
            }
            return ret
        }
    }
}