package com.softmotions.ncms.atm

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.google.inject.Singleton
import com.softmotions.ncms.asm.events.*
import com.softmotions.ncms.events.BasicEvent
import com.softmotions.ncms.events.NcmsEventBus
import com.softmotions.ncms.media.events.MediaDeleteEvent
import com.softmotions.ncms.media.events.MediaMoveEvent
import com.softmotions.ncms.media.events.MediaUpdateEvent
import org.atmosphere.cache.UUIDBroadcasterCache
import org.atmosphere.client.TrackMessageSizeInterceptor
import org.atmosphere.config.service.AtmosphereHandlerService
import org.atmosphere.cpr.*
import org.atmosphere.interceptor.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Ncms admin UI atmosphere intergation layer.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
@AtmosphereHandlerService(path = "/ws/adm/ui",
        interceptors = arrayOf(
                AtmosphereResourceLifecycleInterceptor::class,
                TrackMessageSizeInterceptor::class,
                SuspendTrackerInterceptor::class,
                BroadcastOnPostAtmosphereInterceptor::class,
                HeartbeatInterceptor::class,
                JavaScriptProtocol::class),
        broadcasterCache = UUIDBroadcasterCache::class,
        listeners = arrayOf(AdminUIWS.RSEvents::class))
@JvmSuppressWildcards
open class AdminUIWS
@Inject
constructor(private val mapper: ObjectMapper,
            private val resourceFactory: AtmosphereResourceFactory,
            private val metaBroadcaster: MetaBroadcaster,
            private val ebus: NcmsEventBus) : OnMessageAtmosphereHandler<Any?>() {

    companion object {

        private val log = LoggerFactory.getLogger(AdminUIWS::class.java)

        private val BROADCAST_ALL = "/ws/adm/ui"
    }

    private val ruuid2User = HashMap<String, String>()

    private val user2ruuids = HashMap<String, MutableSet<String>>()

    private val lock = ReentrantLock()

    init {
        log.info("AdminUIWS instantiated")
        ebus.register(this);
    }

    override fun onOpen(resource: AtmosphereResource) = register(resource)

    override fun onDisconnect(response: AtmosphereResponse, event: AtmosphereResourceEvent) = terminate(event.resource)

    override fun onTimeout(response: AtmosphereResponse, event: AtmosphereResourceEvent) = terminate(event.resource)

    private fun register(resource: AtmosphereResource) {
        val uuid = resource.uuid()
        val user = resource.request.wrappedRequest().userPrincipal?.name ?: return run {
            log.warn("Unauthenticated user within 'ws/admin/ui' atmosphere channel. UUID: {}", uuid)
        }
        lock.withLock {
            if (uuid !in ruuid2User) {
                log.info("Register atmosphere resource: {} for user: {}", uuid, user)
            }
            ruuid2User[uuid] = user
            val uuids = user2ruuids.getOrPut(user, {
                HashSet<String>()
            })
            uuids += uuid
        }
    }

    private fun terminate(resource: AtmosphereResource) {
        val uuid = resource.uuid()
        log.info("Timeout/Disconnected uuid={}", uuid)
        val user = lock.withLock {
            val user = ruuid2User.remove(uuid) ?: return@withLock null
            val uuids = user2ruuids[user] ?: return@withLock user
            uuids.remove(uuid)
            if (!uuids.isEmpty()) {
                log.info("Found live uuids for user: {} uuids: {}", user, uuids)
                null
            } else {
                user2ruuids.remove(user)
                user
            }
        } ?: return
        log.info("User: {} disconnected", user)
        ebus.fire(UIUserDisconnectedEvent(user, this))
    }

    @Singleton
    @JvmSuppressWildcards
    open class RSEvents
    @Inject
    constructor(private val aws: AdminUIWS) : AtmosphereResourceEventListenerAdapter() {

        override fun onClose(event: AtmosphereResourceEvent) {
            aws.terminate(event.resource)
        }

        override fun onDisconnect(event: AtmosphereResourceEvent) {
            aws.terminate(event.resource)
        }

        override fun onThrowable(event: AtmosphereResourceEvent) {
            log.error("", event.throwable())
            aws.terminate(event.resource)
        }
    }

    private fun createMessage(evt: BasicEvent): WSMessage {
        return WSMessage(mapper)
                .put("type", evt.type)
                .put("user", evt.user)
                .putPOJO("hints", evt.hints())
    }

    override fun onMessage(response: AtmosphereResponse,
                           data: Any?,
                           event: AtmosphereResourceEvent) {
        data ?: return
        if (data is WSMessage) {
            if (log.isDebugEnabled) {
                log.debug("On server message: {}", data)
            }
            response.writer.write(data.toString())
            return
        }
        ebus.fire(UIUserMessageEvent(
                this,
                WSMessage(mapper, data.toString()),
                event.resource.uuid(),
                BROADCAST_ALL,
                metaBroadcaster,
                resourceFactory,
                event.resource.request))
    }

    ///////////////////////////////////////////////////////////
    //                   Ncms ebus listeners                 //
    ///////////////////////////////////////////////////////////

    private fun isBroadcastAllowed(evt: BasicEvent): Boolean {
        return true != evt.hints()["silent"]
    }

    @Subscribe
    fun onDisconnected(evt: UIUserDisconnectedEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt))
    }

    @Subscribe
    fun onAsmModified(evt: AsmModifiedEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id))
    }

    @Subscribe
    fun onAsmCreatedEvent(evt: AsmCreatedEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id)
                            .put("name", evt.name)
                            .put("hname", evt.hname)
                            .put("navParentId", evt.navParentId)
            )
    }

    @Subscribe
    fun onAsmRemovedEvent(evt: AsmRemovedEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id))
    }

    @Subscribe
    fun onAsmLockedEvent(evt: AsmLockedEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id))
    }

    @Subscribe
    fun onAsmUnlockedEvent(evt: AsmUnlockedEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id))
    }

    @Subscribe
    fun onMediaUpdateEvent(evt: MediaUpdateEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id)
                            .put("isFolder", evt.isFolder)
                            .put("path", evt.path))

    }

    @Subscribe
    fun onMediaDeleteEvent(evt: MediaDeleteEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id)
                            .put("isFolder", evt.isFolder)
                            .put("path", evt.path))
    }

    @Subscribe
    fun onMediaMoveEvent(evt: MediaMoveEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("id", evt.id)
                            .put("isFolder", evt.isFolder)
                            .put("newPath", evt.newPath)
                            .put("oldPath", evt.oldPath))
    }

    @Subscribe
    fun onServerMessage(evt: ServerMessageEvent) {
        if (isBroadcastAllowed(evt))
            metaBroadcaster.broadcastTo(BROADCAST_ALL,
                    createMessage(evt)
                            .put("message", evt.message)
                            .put("error", evt.error)
                            .put("persistent", evt.persistent))

    }

}
