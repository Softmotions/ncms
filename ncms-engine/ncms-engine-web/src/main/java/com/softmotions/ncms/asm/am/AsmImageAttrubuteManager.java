package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        //todo
        return null;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val) {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "width", "height", "resize", "restrict");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        ObjectNode opts = (ObjectNode) val.get("options");
        if (opts == null) {
            opts = mapper.createObjectNode();
        }
        long id = val.has("id") ? val.get("id").asLong() : 0L;
        if (opts.has("resize") && opts.has("width") && opts.get("resize").asBoolean()) {
            int width = opts.get("width").asInt();
            try {
                mediaService.ensureResizedImage(id, width);
            } catch (IOException e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
        }
        attr.setEffectiveValue(val.toString());
        return attr;
    }
}
