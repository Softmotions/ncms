package com.softmotions.ncms.asm.am;

import com.softmotions.commons.json.JsonUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmImageAttrubuteManager implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmImageAttrubuteManager.class);

    public static final String[] TYPES = new String[]{"image"};

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
        attr.setEffectiveValue(val.toString());
        return attr;
    }
}
