package com.softmotions.ncms.mtt.tp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.commons.cont.KVOptions
import com.softmotions.commons.lifecycle.Start
import com.softmotions.commons.re.RegexpHelper
import com.softmotions.kotlin.toDays
import com.softmotions.kotlin.toURLComponent
import com.softmotions.ncms.events.NcmsEventBus
import com.softmotions.web.cookie
import com.softmotions.web.decodeValue
import com.softmotions.web.setEncodedValue
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.commons.collections4.map.Flat3Map
import org.apache.commons.lang3.StringUtils
import org.apache.ibatis.session.SqlSession
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.regex.Pattern
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
@JvmSuppressWildcards
open class MttTpService
@Inject
constructor(val sess: SqlSession,
            val mapper: ObjectMapper,
            val ebus: NcmsEventBus) : MBDAOSupport(MttTpRS::class.java, sess) {

    companion object {

        private val log = LoggerFactory.getLogger(MttTpService::class.java)

        private val MATCHED_TPS_COOKIE_KEY = "_tps";

        private val MATCHED_TPS_REQ_KEY = MttTpRS::class.java.name + "_tps"

        private val replaceUrlRP = Pattern.compile("\\{([A-Za-z_\\.]+)\\}")
    }

    // Parameter name => parameter value => TpSlot
    private val pmap: MutableMap<String, MutableMap<String, TpSlot>> = HashMap()

    // TpSlot id => TpSlot
    private val imap: MutableMap<Long, TpSlot> = HashMap()

    private val lock = ReentrantReadWriteLock()


    init {
        ebus.register(this)
    }

    fun activateTrackingPixels(req: HttpServletRequest,
                               resp: HttpServletResponse,
                               tpNameGlob: String = "*",
                               ctx: Map<String, Any?> = emptyMap(),
                               removeMatched: Boolean = true): Iterable<MttActivatedTp> {

        val itps = loadTrackingPixels(req)
        if (itps.isEmpty) {
            return emptyList()
        }
        val tps = itps.findTps(if (StringUtils.containsAny("?*{}", tpNameGlob)) {
            "^${RegexpHelper.convertGlobToRegEx(tpNameGlob)}$".toRegex()
        } else {
            "^${Pattern.quote(tpNameGlob)}$".toRegex()
        })
        if (tps.isEmpty()) {
            return emptyList()
        }


        fun replace(rctx: Map<String, *>, p: Pattern, data: String, encodeUrl: Boolean): String {
            if (data.isBlank()) {
                return data
            }
            val m = p.matcher(data)
            val sb = StringBuffer(Math.floor(data.length * 1.5).toInt())
            while (m.find()) {
                val nv = rctx[m.group(1)]
                if (nv != null) {
                    m.appendReplacement(sb,
                            if (encodeUrl) nv.toString().toURLComponent()
                            else nv.toString())
                } else {
                    log.error("Parameter: '${m.group(1)}' not found in tracking pixel context: ${data}")
                    m.appendReplacement(sb, m.group())
                }
            }
            m.appendTail(sb)
            return sb.toString()
        }

        val ret = tps.map {

            val nctx: MutableMap<String, Any?> =
                    if (it.tParams.size + ctx.size > 3)
                        HashMap(ctx)
                    else Flat3Map(ctx)

            for ((k, v) in itps.tpmap) {
                if (k.startsWith(it.pkeyPrefix)) {
                    nctx.putIfAbsent(k.substring(it.pkeyPrefix.length), v.firstOrNull())
                }
            }
            if (log.isDebugEnabled) {
                log.debug("activateTrackingPixels ctx {}", nctx)
            }
            val url = replace(nctx, replaceUrlRP, it.url, true)
            val jscode = replace(nctx, replaceUrlRP, it.jscode, false)
            if (removeMatched) {
                itps.removeTp(it)
            }
            if (log.isDebugEnabled) {
                log.debug("Process tp name={} url={} jscode={}", it.name, url, jscode)
            }
            MttActivatedTp(url, jscode)
        }

        itps.writeTo(req, resp)
        return ret
    }

    //
    // 0,=43434,983,9289,32,44
    // name1=pvalue1
    // name2=pvalue2
    //
    internal inner class InjectedTps(cval: String = "") {

        internal var modified = false

        internal val isEmpty: Boolean
            get() = tpmap.isEmpty()

        internal val tpmap: MutableMap<String, MutableCollection<String>>

        init {
            val kvo = KVOptions(cval)
            tpmap = HashMap(kvo.size)
            for ((k, v) in kvo) {
                if (k == "0,") {
                    tpmap[k] = StringUtils.split(v, ',').toMutableSet()
                } else {
                    tpmap[k] = mutableListOf(v)
                }
            }
        }

        internal fun syncTParams(tp: TpSlot, pmap: Map<String, Array<String>>) {
            for (pn in tp.tParams) {
                val pv = pmap[pn]
                if (pv != null && pv.size > 0) { //todo review
                    val parr = tpmap.getOrPut("${tp.pkeyPrefix}${pn}", {
                        modified = true
                        mutableListOf(pv[0])
                    }) as MutableList<String>
                    if (parr.firstOrNull() != pv[0]) {
                        modified = true
                        parr[0] = pv[0]
                    }
                }
            }
        }

        internal fun findTps(pattern: Regex): Collection<TpSlot> {
            val ids = tpmap["0,"] ?: return emptyList()
            lock.read {
                @Suppress("UNCHECKED_CAST")
                return ids
                        .map { imap[it.toLong()] }
                        .filter { it != null && it.enabled && it.name.matches(pattern) }
                        as Collection<TpSlot>
            }
        }

        internal fun removeTp(tp: TpSlot) {
            val sids = tpmap["0,"] ?: return
            val prefix = "${tp.id},"
            if (sids.remove(tp.sid)) {
                modified = true
            }
            for (k in tpmap.keys.toTypedArray()) {
                if (k.startsWith(prefix)) {
                    tpmap.remove(k)
                }
            }
        }

        internal fun addTp(tp: TpSlot, pmap: Map<String, Array<String>>) {
            if (!tp.enabled) {
                return
            }
            val sids = tpmap.getOrPut("0,", {
                modified = true
                ArrayList(8)
            })
            if (!sids.contains(tp.sid)) {
                modified = true
                sids += tp.sid
            }
            syncTParams(tp, pmap)
        }

        internal operator fun contains(tp: TpSlot): Boolean {
            val sids = tpmap["0,"] ?: return false
            return tp.sid in sids
        }

        internal fun writeTo(req: HttpServletRequest, resp: HttpServletResponse) {
            if (!modified) {
                return
            }
            req.setAttribute(MATCHED_TPS_REQ_KEY, this)
            val amap = KVOptions()
            for ((k, v) in tpmap) {
                amap.put(k, v.joinToString(","))
            }
            val cookie = Cookie(MATCHED_TPS_COOKIE_KEY, null)
            if (log.isDebugEnabled) {
                log.debug("Set cookie {}={}", MATCHED_TPS_COOKIE_KEY, amap.toString())
            }
            cookie.setEncodedValue(amap.toString())
            // todo 1 day hardcoded
            cookie.maxAge = 1.toDays().toSeconds().toInt()
            resp.addCookie(cookie)
            modified = false
        }

        override fun toString(): String {
            return "InjectedTps(modified=${modified}, tpmap=${tpmap})"
        }
    }

    private fun loadTrackingPixels(req: HttpServletRequest): InjectedTps {
        val tps = (req.getAttribute(MATCHED_TPS_REQ_KEY) as InjectedTps?)
                ?: InjectedTps(req.cookie(MATCHED_TPS_COOKIE_KEY)?.decodeValue() ?: "")
        if (log.isDebugEnabled) {
            log.debug("loadTrackingPixels={}", tps)
        }
        return tps
    }

    fun injectTrackingPixels(req: HttpServletRequest, resp: HttpServletResponse) {

        val tps = loadTrackingPixels(req)
        val rpMap = req.parameterMap

        lock.read {

            for ((pn, pvs) in rpMap) {

                // Matching against a raw string parameters
                pmap[pn]?.let {
                    for (pv in pvs) {
                        it[pv]?.let {
                            if (log.isDebugEnabled) {
                                log.debug("Tps matched {}={}, tps={}", pn, pv, it)
                            }
                            tps.addTp(it, rpMap)
                        }
                    }
                }

                // Matching against a regexps
                for (pv in pvs) {
                    for (it in imap.values) {
                        val re = it.rParams[pn]
                        if (re == null || !it.enabled) {
                            continue
                        }
                        if (it in tps) {
                            tps.syncTParams(it, rpMap)
                            continue
                        }
                        if (re.matches(pv)) {
                            if (log.isDebugEnabled) {
                                log.debug("Tps matched re={} {}={}, tps={}", re, pn, pv, it)
                            }
                            tps.addTp(it, rpMap)
                        }
                    }
                }
            }
        }

        tps.writeTo(req, resp)
    }

    private fun activateTp(tp: MttTp) {
        val ns = TpSlot(tp, mapper)
        if (log.isDebugEnabled) {
            log.debug("activateTp={}", tp)
        }
        lock.write {
            imap[ns.id]?.let {
                tpDeleted(it.id)
            }
            imap[ns.id] = ns
            for ((pn, pv) in ns.sParams) {
                pmap.getOrPut(pn, {
                    HashMap<String, TpSlot>()
                }).getOrPut(pv, {
                    ns
                })
            }
        }
    }

    private fun activateTp(id: Long) {
        val tp: MttTp? = selectOne("selectTpById", id)
        if (tp != null) {
            activateTp(tp)
        }
    }

    @Subscribe
    fun tpDeleted(ev: MttTpDeletedEvent) {
        tpDeleted(ev.tpId)
    }

    fun tpDeleted(id: Long) {
        lock.write {
            imap.remove(id)?.let {
                for (m in pmap.values) {
                    m.keys.toTypedArray().forEach {
                        val slot = m[it]
                        if (slot!!.id == id) {
                            if (log.isDebugEnabled) {
                                log.debug("tpDeleted={}", it)
                            }
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

    internal class TpSlot(val tp: MttTp, mapper: ObjectMapper) {

        internal val id: Long
            get() = tp.id

        internal val name: String
            get() = tp.name

        internal val sid: String

        internal val enabled: Boolean
            get() = tp.isEnabled

        internal val url: String
            get() = spec.path("url").asText()

        internal val jscode: String
            get() = spec.path("jscode").asText()

        internal val spec: ObjectNode

        internal val pkeyPrefix: String

        // Parameter name => raw parameter string value
        internal val sParams = HashMap<String, String>()

        // Parameter name => parameter regexp value
        internal val rParams = HashMap<String, Regex>()

        // Transferred parameters what will be saved for this TP
        // in the client's cookie
        internal val tParams = HashSet<String>()

        init {

            sid = tp.id.toString()
            pkeyPrefix = "${sid},"
            spec = mapper.readTree(tp.spec) as ObjectNode
            // {"params":"utm_source=yandex","url":"http://sm.ru?","jscode":"eew\nwqwqwwq\n\n\n\n\n"}
            for ((pn, pv) in KVOptions(spec.path("params").asText())) {
                if (StringUtils.containsAny("?*{}", pv)) {
                    rParams[pn] = Regex("^" + RegexpHelper.convertGlobToRegEx(pv) + "$")
                } else {
                    sParams[pn] = pv
                }
            }
            spec.path("tparams").asText().split(',').filter {
                it.isNotBlank()
            }.forEach {
                val pname = it.trim()
                if (!pname.startsWith("0,")) {
                    tParams += pname
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false
            other as TpSlot
            if (tp != other.tp) return false
            return true
        }

        override fun hashCode(): Int {
            return tp.hashCode()
        }

        override fun toString(): String {
            return "TpSlot(tp=$tp)"
        }
    }
}