package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.annotation.Nullable;

import com.softmotions.ncms.asm.Asm;

/**
 * Assembly {@link com.softmotions.ncms.asm.Asm} renderer.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRenderer {

    void renderTemplate(String location, AsmRendererContext ctx, Writer out) throws AsmRenderingException, IOException;

    boolean isHasRenderableAsmAttribute(Asm asm, AsmRendererContext ctx, String name);

    void renderAsm(AsmRendererContext ctx, @Nullable Writer writer) throws AsmRenderingException, IOException;

    Object renderAsmAttribute(AsmRendererContext ctx,
                              String attributeName, Map<String, String> opts) throws AsmRenderingException;
}
