package com.softmotions.ncms.atm;

import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
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

@ManagedService(path = "/admin")
@Singleton
public class AdminUIManagedService {

    private static final Logger log = LoggerFactory.getLogger(AdminUIManagedService.class);

    private final ObjectMapper mapper;

    @Inject
    public AdminUIManagedService(ObjectMapper mapper) {
        this.mapper = mapper;
        log.info("AdminUIManagedService instantiated");
    }

    @Ready
    public WSMessage onReady(final AtmosphereResource r) {
        log.info("onReady uuid={}", r.uuid());
        ObjectNode resp = mapper.createObjectNode();
        resp.put("message", "Hello " + r.uuid());
        return new WSMessage(mapper, resp);
    }

    @Message(encoders = WSMessage.EncoderDecoder.class,
             decoders = WSMessage.EncoderDecoder.class)
    public WSMessage onMessage(WSMessage msg) {
        log.info("onMessage {}", msg);
        ObjectNode resp = mapper.createObjectNode();
        resp.put("message", "Response for " + msg.getUuid());
        return new WSMessage(mapper, resp);
    }
}
