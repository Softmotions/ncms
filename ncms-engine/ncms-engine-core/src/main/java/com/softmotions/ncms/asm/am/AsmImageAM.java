package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.mhttl.Image;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmImageAM implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmImageAM.class);

    public static final String[] TYPES = new String[]{"image"};

    final MediaRepository repository;

    final ObjectMapper mapper;

    @Inject
    public AsmImageAM(MediaRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) {
        return attr;
    }

    public Image renderAsmAttribute(AsmRendererContext ctx, ObjectNode node) {
        Image image = new Image(ctx);
        image.setId(node.hasNonNull("id") ? node.get("id").asLong() : 0);
        if (node.hasNonNull("options")) {
            ObjectNode opts = (ObjectNode) node.get("options");
            if (opts.hasNonNull("width")) {
                image.setOptionsWidth(opts.get("width").asInt());
            }
            if (opts.hasNonNull("height")) {
                image.setOptionsHeight(opts.get("height").asInt());
            }
            if (opts.hasNonNull("resize")) {
                image.setResize(opts.get("resize").asBoolean());
            }
            if (opts.hasNonNull("restrict")) {
                image.setRestrict(opts.get("restrict").asBoolean());
            }
            if (opts.hasNonNull("skipSmall")) {
                image.setSkipSmall(opts.get("skipSmall").asBoolean());
            }
        }
        return image;
    }


    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String value = attr != null ? attr.getEffectiveValue() : null;
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Image res = new Image(ctx);
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            String key;
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return Collections.EMPTY_LIST;
            }
            while (parser.nextValue() != null) {
                key = parser.getCurrentName();
                if ("id".equals(key)) {
                    res.setId(parser.getValueAsLong());
                } else if ("options".equals(key)) {
                    while (parser.nextValue() != null && (key = parser.getCurrentName()) != null) {
                        switch (key) {
                            case "width":
                                res.setOptionsWidth(parser.getValueAsInt());
                                break;
                            case "height":
                                res.setOptionsHeight(parser.getValueAsInt());
                                break;
                            case "resize":
                                res.setResize(parser.getValueAsBoolean());
                                break;
                            case "restrict":
                                res.setRestrict(parser.getValueAsBoolean());
                                break;
                            case "skipSmall":
                                res.setSkipSmall(parser.getValueAsBoolean(true));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "width", "height",
                                        "resize", "restrict", "skipSmall");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
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
                repository.ensureResizedImage(id, width, height, skipSmall);
            } catch (IOException e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
        }
        attr.setEffectiveValue(val.toString());
        return attr;
    }

    public void attributePersisted(AsmAttribute attr, JsonNode val, HttpServletRequest req) {

    }

}
