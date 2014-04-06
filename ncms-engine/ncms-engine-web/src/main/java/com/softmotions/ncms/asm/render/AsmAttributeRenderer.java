package com.softmotions.ncms.asm.render;

import java.util.Map;

/**
* @author Adamansky Anton (adamansky@gmail.com)
*/
public interface AsmAttributeRenderer {

    String[] getSupportedAttributeTypes();

    String renderAsmAttribute(AsmRendererContext ctx, String attrname,
                              Map<String, String> options) throws AsmRenderingException;
}
