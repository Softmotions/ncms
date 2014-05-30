package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.AsmAttribute;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmAttributeManager {

    String[] getSupportedAttributeTypes();

    String renderAsmAttribute(AsmRendererContext ctx, String attrname,
                              Map<String, String> options) throws AsmRenderingException;

    AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode options);

    AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode value);

}
