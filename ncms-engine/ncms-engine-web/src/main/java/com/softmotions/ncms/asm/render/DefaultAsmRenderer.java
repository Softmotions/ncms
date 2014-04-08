package com.softmotions.ncms.asm.render;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@SuppressWarnings("unchecked")
public class DefaultAsmRenderer implements AsmRenderer {

    private static final Logger log = LoggerFactory.getLogger(DefaultAsmRenderer.class);

    /**
     * Set of template engines.
     */
    final Set<AsmTemplateEngineAdapter> templateEgines;

    /**
     * Set of attribute renderers
     */
    final Set<AsmAttributeRenderer> attributeRenderers;

    /**
     * Set type => AsmAttributeRenderer
     */
    final Map<String, AsmAttributeRenderer> typeAttributeRenderersMap;


    @Inject
    public DefaultAsmRenderer(Set<AsmTemplateEngineAdapter> templateEgines,
                              Set<AsmAttributeRenderer> attributeRenderers) {
        this.templateEgines = templateEgines;
        this.attributeRenderers = attributeRenderers;
        this.typeAttributeRenderersMap = new HashMap<>();
        for (final AsmAttributeRenderer ar : attributeRenderers) {
            for (final String atype : ar.getSupportedAttributeTypes()) {
                typeAttributeRenderersMap.put(atype, ar);
            }
        }
    }

    public void renderAsm(AsmRendererContext ctx) throws AsmRenderingException, IOException {
        Asm asm = ctx.getAsm();
        AsmCore core = asm.getEffectiveCore();
        if (core == null) {
            throw new AsmRenderingException("Missing core for assembly: " + asm.getName());
        }
        ctx.push();
        try {
            if (executeAsmHandler(asm, ctx) || ctx.getServletResponse().isCommitted()) {
                return; //Assembly handler took full control on response
            }
            AsmTemplateEngineAdapter te = selectTemplateEngineForCore(core);
            if (te == null) {
                throw new AsmRenderingException("Failed to select template engine for assembly " +
                                                "core: " + core + " assembly: " + asm.getName());
            }
            if (log.isTraceEnabled()) {
                log.trace("Selected template engine: " + te.getClass().getName() +
                          " assembly: " + asm.getName());
            }

            te.renderTemplate(core.getLocation(), ctx, ctx.getServletResponse().getWriter());
        } finally {
            ctx.pop();
        }
    }

    public String renderAsmAttribute(AsmRendererContext ctx, String attributeName,
                                     Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attributeName);
        if (attr == null) {
            log.warn("Acquired attribute: " + attributeName +
                     " not found in assembly: " + asm.getName());
            return "";
        }
        String type = attr.getType();
        if (type == null) {
            type = "*";
        }
        AsmAttributeRenderer arend = typeAttributeRenderersMap.get(type);
        if (arend == null) {
            arend = typeAttributeRenderersMap.get("*");
        }
        if (arend == null) {
            throw new AsmRenderingException("Unable to find attribute renderer for: " +
                                            attr);
        }
        return arend.renderAsmAttribute(ctx, attr.getName(), options);
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

    protected AsmTemplateEngineAdapter selectTemplateEngineForCore(AsmCore core) {
        AsmTemplateEngineAdapter defaultTe = null;
        String type = core.getTemplateEngine();
        if (StringUtils.isBlank(type)) {
            type = FilenameUtils.getExtension(core.getLocation());
        }
        if (StringUtils.isBlank(type)) {
            type = "*";
        }
        String dotType = '.' + type;
        for (final AsmTemplateEngineAdapter te : templateEgines) {
            if (defaultTe == null) {
                defaultTe = te;
            }
            for (String ext : te.getSupportedExtensions()) {
                if ("*".equals(ext) || ".*".equals(ext)) {
                    defaultTe = te;
                }
                if (ext.equals(type) || ext.equals(dotType)) {
                    return te;
                }
            }
        }
        return defaultTe;
    }
}
