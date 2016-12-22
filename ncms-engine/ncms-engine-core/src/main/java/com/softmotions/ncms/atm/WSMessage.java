package com.softmotions.ncms.atm;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class WSMessage {

    private static final Logger log = LoggerFactory.getLogger(WSMessage.class);

    private final String uuid;

    private final ObjectNode data;

    private final ObjectMapper mapper;

    public WSMessage(ObjectMapper mapper, ObjectNode onode) {
        this.mapper = mapper;
        this.data = onode;
        if (!data.path("uuid").isTextual()) {
            uuid = UUID.randomUUID().toString();
            data.put("uuid", uuid);
        } else {
            this.uuid = data.path("uuid").asText();
        }
    }

    public WSMessage(ObjectMapper mapper, String sdata) throws IOException {
        this(mapper, (ObjectNode) mapper.readTree(sdata));
    }

    public WSMessage(ObjectMapper mapper) {
        this.mapper = mapper;
        uuid = UUID.randomUUID().toString();
        data = mapper.createObjectNode();
        data.put("uuid", uuid);
    }


    public String getUuid() {
        return uuid;
    }

    public ObjectNode getData() {
        return data;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String getType() {
        return data.path("type").asText();
    }

    public WSMessage put(String key, String value) {
        data.put(key, value);
        return this;
    }

    public WSMessage put(String key, Boolean value) {
        data.put(key, value);
        return this;
    }

    public WSMessage put(String key, Long value) {
        data.put(key, value);
        return this;
    }

    public WSMessage putPOJO(String key, Object value) {
        data.putPOJO(key, value);
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WSMessage that = (WSMessage) o;
        return Objects.equals(uuid, that.uuid);
    }

    public int hashCode() {
        return Objects.hash(uuid);
    }

    public String toString() {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class EncoderDecoder implements Decoder<String, WSMessage>, Encoder<WSMessage, String> {

        private final ObjectMapper mapper;

        @Inject
        public EncoderDecoder(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public String encode(WSMessage m) {
            return m.toString();
        }

        @Override
        public WSMessage decode(String s) {
            try {
                return new WSMessage(mapper, s);
            } catch (IOException e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
        }
    }
}
