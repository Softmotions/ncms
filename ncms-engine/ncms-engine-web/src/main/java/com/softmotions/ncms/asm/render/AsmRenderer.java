package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;

/**
 * Assembly {@link com.softmotions.ncms.asm.Asm} renderer.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRenderer {

    void render(AsmRendererContext ctx, Writer out) throws AsmRenderingException, IOException;
}
