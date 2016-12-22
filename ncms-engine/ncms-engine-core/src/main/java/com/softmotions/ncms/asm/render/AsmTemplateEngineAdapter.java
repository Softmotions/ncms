package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import com.softmotions.ncms.asm.render.httl.AsmTemplateSyntaxException;

/**
 * Adapter to the template engine.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface AsmTemplateEngineAdapter {

    String getType();

    /**
     * List of extensions of supported templates.
     */
    String[] getSupportedExtensions();

    /**
     * Render the specified template file.
     */
    void renderTemplate(String location, AsmRendererContext ctx, Writer out) throws IOException;

    void renderTemplate(String location, Map<String, Object> ctx, Locale locale, Writer out) throws IOException;

    /**
     * Checks syntax of a template specified by location.
     *
     * @throws IOException                If template is not found or IO error
     * @throws AsmTemplateSyntaxException If template has syntax errors
     */
    void checkTemplateSyntax(String location) throws AsmTemplateSyntaxException, IOException;


}
