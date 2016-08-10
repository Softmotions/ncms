package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter to the template engine.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface AsmTemplateEngineAdapter {

    /**
     * List of extensions of supported templates.
     */
    String[] getSupportedExtensions();

    /**
     * Render the specified template file.
     */
    void renderTemplate(String location, AsmRendererContext ctx, Writer out) throws IOException;

    void renderTemplate(String location, Map<String, Object> ctx, Locale locale, Writer out) throws IOException;

}
