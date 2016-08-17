package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.cont.Pair;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.mhttl.Image;
import com.softmotions.ncms.mhttl.ImageMeta;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class AsmImageAM extends AsmAttributeManagerSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmImageAM.class);

    public static final String[] TYPES = new String[]{"image"};

    final MediaRepository repository;

    final ObjectMapper mapper;

    @Inject
    public AsmImageAM(MediaRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        String value = attr.getEffectiveValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        ImageMeta meta = new ImageMeta();
        if (!parseImageMeta(value, meta)) {
            return null;
        }
        return meta.getId() != null ? new Object[]{meta} : null;
    }

    public Image renderAsmAttribute(AsmRendererContext ctx, ObjectNode node) {
        Long id = node.hasNonNull("id") ? node.get("id").asLong() : null;
        if (id == null || id == 0L) {
            return null;
        }
        Image image = new Image(ctx);
        image.setId(id);
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
            if (opts.hasNonNull("cover")) {
                image.setCover(opts.get("cover").asBoolean());
            }
        }
        return image;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String value = attr != null ? attr.getEffectiveValue() : null;
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Image res = new Image(ctx);
        if (!parseImageMeta(value, res)) {
            return null;
        }

        if (res.getId() == null || res.getId() == 0L) {
            return null;
        }
        return res;
    }

    private boolean parseImageMeta(String value, ImageMeta res) {
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            String key;
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return false;
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
                            case "cover":
                                res.setCover(parser.getValueAsBoolean());
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
        return true;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "width", "height",
                                        "resize", "restrict", "skipSmall",
                                        "cover");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(val != null ? applyJSONAttributeValue(ctx, attr, val).toString() : null);
        return attr;
    }

    public JsonNode applyJSONAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) {
        ObjectNode opts = (ObjectNode) val.get("options");
        if (opts == null) {
            opts = mapper.createObjectNode();
            ((ObjectNode) val).set("options", opts);
        }
        Long id = val.hasNonNull("id") ? val.get("id").asLong() : null;
        if (id == null) {
            return val;
        }

        ctx.registerMediaFileDependency(attr, id);

        if (((opts.hasNonNull("resize") && opts.get("resize").asBoolean()) ||
             (opts.hasNonNull("cover") && opts.get("cover").asBoolean()))
            &&
            (opts.hasNonNull("width") || opts.hasNonNull("height"))) {

            Integer width = opts.hasNonNull("width") ? opts.get("width").asInt() : 0;
            Integer height = opts.hasNonNull("height") ? opts.get("height").asInt() : 0;
            if (width == 0) {
                width = null;
            }
            if (height == 0) {
                height = null;
            }
            int flags = 0;
            if (width != null && height != null &&
                opts.hasNonNull("cover") && opts.get("cover").asBoolean()) {
                flags |= MediaRepository.RESIZE_COVER_AREA;
            } else if (opts.hasNonNull("skipSmall") && opts.get("skipSmall").asBoolean()) {
                flags |= MediaRepository.RESIZE_SKIP_SMALL;
            }
            try {
                Pair<Integer, Integer> dim = repository.ensureResizedImage(id, width, height, flags);
                if (dim == null) {
                    throw new RuntimeException("Unable to resize image file: " + id +
                                               " width=" + width +
                                               " heigth=" + height + " flags=" + flags);
                }
                opts.set("width", dim.getOne() != null ? opts.numberNode(dim.getOne()) : null);
                opts.set("height", dim.getTwo() != null ? opts.numberNode(dim.getTwo()) : null);

            } catch (IOException e) {
                String msg = "Unable to resize image file: " + id +
                             " width=" + width +
                             " heigth=" + height + " flags=" + flags;
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        return val;
    }
}
