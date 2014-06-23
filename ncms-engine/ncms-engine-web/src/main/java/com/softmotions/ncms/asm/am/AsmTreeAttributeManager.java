package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Tree strucrure attribute manager.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmTreeAttributeManager implements AsmAttributeManager {

    private static final String[] TYPES = new String[]{"tree"};

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm template, AsmAttribute tmplAttr, AsmAttribute attr) {
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            attr.setEffectiveValue("[]");
        }
        return attr;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        return null;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val) {
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val) {
        return attr;
    }
}
