package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Select box controller
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmSelectAttributeManager implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmSelectAttributeManager.class);

    public static final String[] TYPES = new String[]{"select"};

    private final ObjectMapper mapper;

    @Inject
    public AsmSelectAttributeManager(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        String value = attr.getValue();
        if (StringUtils.isEmpty(value)) {
            return Collections.EMPTY_LIST;
        }
        boolean first = BooleanUtils.toBoolean(options.get("first"));
        boolean all = BooleanUtils.toBoolean(options.get("all"));
        List<SelectNode> nodes = first ? null : new ArrayList<SelectNode>();
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                return Collections.EMPTY_LIST;
            }
            while (parser.nextToken() == JsonToken.START_ARRAY) {
                boolean selected = parser.nextBooleanValue();
                String key = parser.nextTextValue();
                String val = parser.nextTextValue();
                if (key != null) {
                    if (selected || all) {
                        if (first) {
                            return new SelectNode(key, val, selected);
                        } else {
                            nodes.add(new SelectNode(key, val, selected));
                        }
                    }
                }
                parser.nextToken(); //should be END_ARRAY
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return nodes;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val) {
        //options
        JsonNode optsVal = val.get("display");
        AsmOptions opts = new AsmOptions();
        if (optsVal.isTextual()) {
            opts.put("display", optsVal.asText());
        }
        optsVal = val.get("multiselect");
        if (optsVal.isBoolean()) {
            opts.put("multiselect", optsVal.asBoolean());
        }

        attr.setOptions(opts.toString());
        applyAttributeValue(attr, val);

        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        JsonNode value = val.get("value");
        if (value != null && value.isContainerNode()) {
            attr.setEffectiveValue(value.toString());
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    public static final class SelectNode {

        final String key;

        final String value;

        final boolean selected;

        public SelectNode(String key, String value, boolean selected) {
            this.key = key;
            this.value = value;
            this.selected = selected;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public boolean isSelected() {
            return selected;
        }
    }
}
