package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaService;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmImageAttrubuteManager implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmImageAttrubuteManager.class);

    public static final String[] TYPES = new String[]{"image"};

    final MediaService mediaService;

    final ObjectMapper mapper;

    @Inject
    public AsmImageAttrubuteManager(MediaService mediaService, ObjectMapper mapper) {
        this.mediaService = mediaService;
        this.mapper = mapper;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm template, AsmAttribute tmplAttr, AsmAttribute attr) {
        return attr;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String value = attr != null ? attr.getEffectiveValue() : null;
        if (StringUtils.isBlank(value)) {
            return null;
        }
        AsmImageNode res = new AsmImageNode();
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            JsonToken t;
            String key;
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return Collections.EMPTY_LIST;
            }
            t = parser.nextValue();
            if (t == null) {
                return res;
            }
            key = parser.getCurrentName();
            if ("id".equals(key)) {
                res.id = parser.getValueAsLong();
            } else if ("options".equals(key)) {
                while ((t = parser.nextValue()) != null) {
                    key = parser.getCurrentName();
                    switch (key) {
                        case "width":
                            res.optionsWidth = parser.getValueAsInt();
                            break;
                        case "height":
                            res.optionsHeight = parser.getValueAsInt();
                            break;
                        case "resize":
                            res.resize = parser.getValueAsBoolean();
                            break;
                        case "restrict":
                            res.restrict = parser.getValueAsBoolean();
                            break;
                        case "skipSmall":
                            res.skipSmall = parser.getValueAsBoolean(true);
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val) {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "width", "height",
                                        "resize", "restrict", "skipSmall");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        ObjectNode opts = (ObjectNode) val.get("options");
        if (opts == null) {
            opts = mapper.createObjectNode();
        }
        long id = val.hasNonNull("id") ? val.get("id").asLong() : 0L;
        if (opts.hasNonNull("resize") && opts.get("resize").asBoolean() &&
            (opts.hasNonNull("width") || opts.hasNonNull("height"))) {
            Integer width = opts.hasNonNull("width") ? opts.get("width").asInt() : 0;
            Integer height = opts.hasNonNull("height") ? opts.get("height").asInt() : 0;
            if (width.intValue() == 0) {
                width = null;
            }
            if (height.intValue() == 0) {
                height = null;
            }
            boolean skipSmall = !opts.hasNonNull("skipSmall") || opts.get("skipSmall").asBoolean(true);
            try {
                mediaService.ensureResizedImage(id, width, height, skipSmall);
            } catch (IOException e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
        }
        attr.setEffectiveValue(val.toString());
        return attr;
    }

    public static final class AsmImageNode {

        long id;
        boolean restrict;
        boolean resize;
        boolean skipSmall;
        Integer optionsWidth;
        Integer optionsHeight;

        public long getId() {
            return id;
        }

        public boolean isRestrict() {
            return restrict;
        }

        public boolean isResize() {
            return resize;
        }

        public boolean isSkipSmall() {
            return skipSmall;
        }

        public Integer getOptionsWidth() {
            return optionsWidth;
        }

        public Integer getOptionsHeight() {
            return optionsHeight;
        }

        public String getLink() {
            //todo !!!!
            return "" + id;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder("AsmImageNode{");
            sb.append("id=").append(id);
            sb.append(", optionsWidth=").append(optionsWidth);
            sb.append(", optionsHeight=").append(optionsHeight);
            sb.append(", restrict=").append(restrict);
            sb.append(", resize=").append(resize);
            sb.append(", skipSmall=").append(skipSmall);
            sb.append('}');
            return sb.toString();
        }
    }
}
