package com.softmotions.ncms.atm;

import java.io.IOException;

import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Ncms admin UI websocket events handler.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
@AtmosphereHandlerService(path = "/ws/adm/ui",
                          interceptors = {AtmosphereResourceLifecycleInterceptor.class,
                                          TrackMessageSizeInterceptor.class,
                                          SuspendTrackerInterceptor.class,
                                          BroadcastOnPostAtmosphereInterceptor.class},
                          broadcasterCache = UUIDBroadcasterCache.class)

public class AdminUIWS extends OnMessageAtmosphereHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(AdminUIWS.class);

    private final ObjectMapper mapper;

    private final AtmosphereResourceFactory resourceFactory;

    @Inject
    public AdminUIWS(ObjectMapper mapper,
                     AtmosphereResourceFactory resourceFactory) {
        this.mapper = mapper;
        this.resourceFactory = resourceFactory;
        log.info("AdminUIWS instantiated");
    }

    @Override
    public void onOpen(AtmosphereResource resource) throws IOException {
        log.info("Opened uuid={}", resource.uuid());
    }

    @Override
    public void onDisconnect(AtmosphereResponse response) throws IOException {
        log.info("Disconected uuid={}", response.uuid());
    }

    @Override
    public void onTimeout(AtmosphereResponse response) throws IOException {
        log.info("Timeout uuid={}", response.uuid());
    }

    @Override
    public void onMessage(AtmosphereResponse response,
                          String data,
                          AtmosphereResourceEvent event) throws IOException {
        WSMessage msg = new WSMessage(mapper, data);
        ObjectNode resp = mapper.createObjectNode();
        resp.put("message", "Response for " + msg.getUuid());
        response.getWriter().write(new WSMessage(mapper, resp).toString());
    }
}
