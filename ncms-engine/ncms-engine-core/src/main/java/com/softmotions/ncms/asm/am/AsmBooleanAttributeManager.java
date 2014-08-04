package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmBooleanAttributeManager implements AsmAttributeManager {

    public static final String[] TYPES = new String[]{"boolean"};

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm template, AsmAttribute tmplAttr, AsmAttribute attr) {
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            attr.setEffectiveValue("false");
        }
        return attr;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        return BooleanUtils.toBooleanObject(attr.getValue());
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "display");
        attr.setOptions(asmOpts.toString());
        return applyAttributeValue(attr, val, req);
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
        JsonNode n = val.get("value");
        if (n != null && BooleanUtils.toBoolean(n.asText())) {
            attr.setEffectiveValue("true");
        } else {
            attr.setEffectiveValue("false");
        }
        return attr;
    }
}
