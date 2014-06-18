package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Select box controller
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */


public class AsmSelectBoxAttributeManager implements AsmAttributeManager {

    public static final String[] TYPES = new String[]{"select"};

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }


    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);


        return null;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode options) {
        return null;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode value) {
        return null;
    }
}
