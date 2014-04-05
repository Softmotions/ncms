package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;

import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@SuppressWarnings("unchecked")
public class DefaultAsmRenderer implements AsmRenderer {

    private static final Logger log = LoggerFactory.getLogger(DefaultAsmRenderer.class);


    public void render(AsmRendererContext ctx, Writer out) throws AsmRenderingException, IOException {
        Asm asm = ctx.getAsm();
        AsmCore core = asm.getEffectiveCore();
        if (core == null) {
            throw new AsmRenderingException("Missing core for assembly: " + asm.getName());
        }
        if (executeAsmHandler(asm, ctx) || ctx.getServletResponse().isCommitted()) {
            return; //Assembly handler took full control on response
        }


        //out.write("ASM=" + asm);
        out.write("ASM=" + asm);
    }


    /**
     * Executes optional assembly handler.
     *
     * @return True if handler commits the response.
     */
    protected boolean executeAsmHandler(Asm asm, AsmRendererContext ctx) {
        AsmAttribute hattr = asm.getAttribute(Asm.ASM_HANDLER_CLASS_ATTR_NAME);
        if (hattr == null) {
            return false;
        }
        String handlerClassName = hattr.getValue();
        if (StringUtils.isBlank(handlerClassName)) {
            return false;
        }

        Class handlerClass;
        try {
            handlerClass = ctx.getClassLoader().loadClass(handlerClassName);
            if (!AsmHandler.class.isAssignableFrom(handlerClass)) {
                throw new AsmRenderingException("AsmHandler: '" + handlerClassName + "' " +
                                                "' class does not implement: " + AsmHandler.class.getName() +
                                                " interface for assembly: " + asm.getName());
            }
        } catch (ClassNotFoundException e) {
            log.error("", e);
            throw new AsmRenderingException("AsmHandler class: '" + handlerClassName +
                                            "' not found for assembly: " + asm.getName());
        }

        try {
            AsmHandler handler = (AsmHandler) ctx.getInjector().getInstance(handlerClass);
            return handler.execute(ctx);
        } catch (ClassNotFoundException e) {
            log.error("", e);
            throw new AsmRenderingException("AsmHandler class: '" + handlerClassName +
                                            "' not found for assembly: " + asm.getName());
        } catch (Exception e) {
            log.error("", e);
            throw new AsmRenderingException("Failed to execute assembly handler: '" + handlerClassName +
                                            "' for assembly: " + asm.getName());
        }
    }

    protected AsmTemplateEngineAdapter findTemplateEngineAdapter(AsmCore core, AsmRendererContext ctx) {
        return null;
    }
}
