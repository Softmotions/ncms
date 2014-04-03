package com.softmotions.ncms.asm.render;

import java.io.Writer;

/**
 * Assembly attribute {@link com.softmotions.ncms.asm.AsmAttribute} renderer.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmAttributeRenderer {

    void render(AsmAttributeRendererContext ctx, Writer out) throws AsmRenderingException;
}
