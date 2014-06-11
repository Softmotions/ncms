package com.softmotions.ncms.asm.render;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmStringAttributeManager implements AsmAttributeManager {

    public static final String[] TYPES = new String[]{"*", "string"};

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public String renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || attr.getEffectiveValue() == null) {
            return null;
        }
        return attr.getEffectiveValue();
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val) {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "display", "placeholder");
        attr.setOptions(asmOpts.toString());
        attr.setEffectiveValue(val.hasNonNull("value") ? val.get("value").asText() : null);
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        attr.setEffectiveValue(val.has("value") ? val.get("value").asText() : null);
        return attr;
    }
}
