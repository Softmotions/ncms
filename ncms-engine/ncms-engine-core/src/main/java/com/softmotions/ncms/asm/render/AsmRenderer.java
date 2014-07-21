package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Assembly {@link com.softmotions.ncms.asm.Asm} renderer.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRenderer {

    void renderTemplate(String location, AsmRendererContext ctx, Writer out) throws AsmRenderingException, IOException;

    void renderAsm(AsmRendererContext ctx) throws AsmRenderingException, IOException;

    Object renderAsmAttribute(AsmRendererContext ctx,
                              String attributeName, Map<String, String> opts) throws AsmRenderingException;
}
