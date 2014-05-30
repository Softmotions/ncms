package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmStringAttributeManager implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmStringAttributeManager.class);

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

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode opts) {
        //opts={"display":"field","value":"нлеглгн"}
        AsmOptions asmOpts = new AsmOptions();


        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        return attr;
    }
}
