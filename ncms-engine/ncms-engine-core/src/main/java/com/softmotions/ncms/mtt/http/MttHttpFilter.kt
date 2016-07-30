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
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
open class MttHttpFilter
@Inject
constructor(val ebus: NcmsEventBus,
            val mapper: ObjectMapper,
            val env: NcmsEnvironment,
            filterHandlers: MttFilterHandlers,
            actionHandlers: MttActionHandlers,
            sess: SqlSession) : MBDAOSupport(MttHttpFilter::class.java, sess), Filter {

    companion object {

        val MTT_RIDS_KEY = "MTTR"

        private val log: Logger = LoggerFactory.getLogger(MttHttpFilter::class.java);
    }

    /**
     * Dynamic resources URI patterns
     */
    private val DEFAULT_DYN_RE = Regex("^((.*\\.(jsp|htm|html|httl|vm|ftl))|([^\\.]+))$", RegexOption.IGNORE_CASE)

    private val lock = ReentrantReadWriteLock()

    private val filterHandlers: Map<String, MttFilterHandler> = HashMap<String, MttFilterHandler>().apply {
        filterHandlers.filters.forEach {
            log.info("Register MTT filter: '${it.type}' class: ${it.javaClass.name}")
            put(it.type, it)
        }
    }

    private val actionHandlers: Map<String, MttActionHandler> = HashMap<String, MttActionHandler>().apply {
        actionHandlers.actions.forEach {
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
        if (!processRules(req as HttpServletRequest, resp as HttpServletResponse)) {
            chain!!.doFilter(req, resp);
        }
    }

    override fun destroy() {
        ebus.unregister(this)
    }

    /**
     * Returns `true` if request completely handled by some rule action
     */
    private fun processRules(req: HttpServletRequest, resp: HttpServletResponse): Boolean {
        var ret = false
        if (req.getAttribute(MTT_RIDS_KEY) != null) {
            return false
        }
        val uri = req.requestURI
        // If admin resource
        // OR non dynamic resource
        // OR /rs/media
        // OR /rs/adm
        if (uri.startsWith(env.ncmsAdminRoot)
                || uri.startsWith("/rs/adm")
                || uri.startsWith("/rs/media")
                || !DEFAULT_DYN_RE.matches(uri)) {
            return false;
        }

        val ridlist = ArrayList<Long>(3)
        try {
            // URI=/ URL=http://vk.smsfinance.ru:9191/ QS=test=foo
            // URI=/rs/media/fileid/286 URL=http://vk.smsfinance.ru:9191/rs/media/fileid/286 QS=w=300&h=300
            // URI=/rs/adm/ws/state URL=http://localhost:9191/rs/adm/ws/state QS=nocache=1469768085685
            //log.info("URI=${req.requestURI} URL=${req.requestURL} QS=${req.queryString}")
            lock.read {
                for (rs in id2slots.values) {
                    if (rs.runRule(req, resp)) {
                        ret = true
                        break
                    }
                }
            }
        } finally {
            val rids = ridlist.joinToString()
            req.setAttribute(MTT_RIDS_KEY, rids)
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
        log.info("Rule deleted=${event}")
        activateRule(event.ruleId)

    }

    @Subscribe
    fun onRuleDeleted(event: MttRuleDeletedEvent) {
        log.info("Rule deleted=${event}")
        lock.write {
            id2slots.remove(event.ruleId)
        }
    }

    @Subscribe
    fun onRuleReordered(event: MttRuleReorderedEvent) {
        log.info("Rule reordered=${event}")
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
        fun runRule(req: HttpServletRequest, resp: HttpServletResponse): Boolean {
            // Run filters
            filters.find {
                try {
                    return@find it.handler.matched(it, req)
                } catch(e: Throwable) {
                    log.error("Rule filter error ${it.javaClass}", e)
                    return@find false
                }
            } ?: return false
            // All filters matched, now run actions
            return actions.find {
                if (it.action.groupId != null) {
                    // this action will be runned by the special MttGroupActionHandler
                    return false
                }
                it.execute(req, resp)
            } == null
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
                it.action.groupId == action.id
            }
        }

        override fun execute(req: HttpServletRequest, resp: HttpServletResponse): Boolean {
            try {
                return handler.execute(this, req, resp)
            } catch(e: Throwable) {
                log.error("Rule action error ${handler.javaClass}", e)
                return false
            }
        }
    }
}