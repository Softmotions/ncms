package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmAttributeManager {

    String[] getSupportedAttributeTypes();

    AsmAttribute prepareGUIAttribute(Asm page, Asm template,
                                     AsmAttribute tmplAttr,
                                     AsmAttribute attr);

    Object renderAsmAttribute(AsmRendererContext ctx,
                              String attrname,
                              Map<String, String> options) throws AsmRenderingException;

    AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx,
                                       AsmAttribute attr,
                                       JsonNode val);

    AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx,
                                     AsmAttribute attr,
                                     JsonNode val);

    void attributePersisted(AsmAttributeManagerContext ctx,
                            AsmAttribute attr,
                            JsonNode val);

}
