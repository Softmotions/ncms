package com.softmotions.ncms.asm;

import java.io.Writer;

/**
 * Renderable assembly component.
 * It can be {@link com.softmotions.ncms.asm.Asm assembly} itself
 * of {@link com.softmotions.ncms.asm.AsmAttribute assembly attribute}.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmRenderable {

    void render(AsmRenderingContext ctx, Writer out) throws AsmRenderingException;
}
