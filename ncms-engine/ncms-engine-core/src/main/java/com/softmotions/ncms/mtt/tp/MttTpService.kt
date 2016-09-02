package com.softmotions.ncms.mtt.tp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.commons.lifecycle.Start
import com.softmotions.ncms.events.NcmsEventBus
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.ibatis.session.SqlSession
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.servlet.http.HttpServletRequest
import kotlin.concurrent.write

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@JvmSuppressWildcards
open class MttTpService
@Inject
constructor(val sess: SqlSession,
            val mapper: ObjectMapper,
            val ebus: NcmsEventBus) : MBDAOSupport(MttTpRS::class.java, sess) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val pmap: MutableMap<String, MutableMap<String, TpSlot>> = HashMap()

    private val imap: MutableMap<Long, TpSlot> = HashMap()

    private val lock = ReentrantReadWriteLock()


    fun handleRequest(req: HttpServletRequest) {

    }

    fun activateTp(tp: MttTp) {
        val nslot = TpSlot(tp)
        lock.write {
            val oslot = imap.get(nslot.tp.id)
            // todo
        }
    }

    fun activateTp(id: Long) {
        val tp: MttTp? = selectOne("selectTpById", id)
        if (tp != null) {
            activateTp(tp)
        }
    }

    @Subscribe
    fun tpDeleted(ev: MttTpDeleteEvent) {
        lock.write {
            imap.remove(ev.tpId)?.let {
                for (m in pmap.values) {
                    m.keys.toTypedArray().forEach {
                        val slot = m[it]
                        if (slot!!.tp.id == ev.tpId) {
                            m.remove(it)
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    fun tpUpdated(ev: MttTpUpdatedEvent) = activateTp(ev.tpId)

    @Start(order = 100)
    fun start() {
        for (tp in select<MttTp>("selectAll")) {
            activateTp(tp)
        }
    }

    inner class TpSlot(val tp: MttTp) {

        val spec: ObjectNode

        init {
            spec = mapper.readTree(tp.spec) as ObjectNode
        }
    }
}