package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }


    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);


        return null;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val) {
        JsonNode value = val.get("value");
        if (value.isContainerNode()) {
            attr.setEffectiveValue(value.toString());
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        log.info("val val=" + val);
        return attr;
    }
}
