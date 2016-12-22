package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import com.softmotions.ncms.asm.Asm;

/**
 * Assembly {@link com.softmotions.ncms.asm.Asm} renderer.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface AsmRenderer {

    void renderTemplate(String location, AsmRendererContext ctx, Writer out) throws AsmRenderingException, IOException;

    /**
     * Renders template specified by location.
     * If template engine not found for specific template extension
     * this method returns `false`
     *
     * @param location Template location
     * @param ctx      Template render context
     * @param locale   Optional locale
     * @param out      Output writer
     * @return True if template was rendered successfully
     * @throws AsmRenderingException
     * @throws IOException
     */
    boolean renderTemplate(String location,
                           Map<String, Object> ctx,
                           @Nullable Locale locale,
                           Writer out) throws IOException;


    boolean isHasSpecificTemplateEngineForLocation(String location);

    boolean isHasRenderableAsmAttribute(Asm asm,
                                        AsmRendererContext ctx,
                                        String name);

    void renderAsm(AsmRendererContext ctx,
                   @Nullable Writer writer) throws AsmRenderingException, IOException;

    @Nullable
    Object renderAsmAttribute(AsmRendererContext ctx,
                              String attributeName,
                              @Nullable Map<String, String> opts) throws AsmRenderingException;
}
