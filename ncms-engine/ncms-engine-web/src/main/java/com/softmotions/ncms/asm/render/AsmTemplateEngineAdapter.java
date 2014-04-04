package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;

/**
 * Adapter to the template engine.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmTemplateEngineAdapter {

    /**
     * List of extensions of supported templates.
     */
    String[] getSupportedCoreExtensions();

    /**
     * Perform template rendering with plugged
     * {@link AttributeRendererCallback}
     * for rendering assembly attributes.
     *
     * @param location Template location.
     * @param ctx      Assembly rendering context
     * @param callback
     * @param out
     * @throws IOException
     */
    void renderAsmCore(String location, AsmRendererContext ctx,
                       AttributeRendererCallback callback, Writer out) throws IOException;

}
