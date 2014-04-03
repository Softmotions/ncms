package com.softmotions.ncms.asm.render;

/**
 * Rendering context for assembly attribute renderers.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmAttributeRendererContext extends AsmRendererContext {

    /**
     * Returns name of rendered attribute.
     */
    String getAttributeName();
}
