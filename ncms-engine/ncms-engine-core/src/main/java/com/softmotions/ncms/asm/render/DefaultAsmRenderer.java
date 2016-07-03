package com.softmotions.ncms.asm.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;

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
    private final Set<AsmTemplateEngineAdapter> templateEgines;

    private final AsmAttributeManagersRegistry amRegistry;

    private final PageService pageService;

    private final AsmRendererHelper helper;


    @Inject
    public DefaultAsmRenderer(Set<AsmTemplateEngineAdapter> templateEgines,
                              AsmAttributeManagersRegistry amRegistry,
                              PageService pageService,
                              AsmRendererHelper helper) {
        this.templateEgines = templateEgines;
        this.amRegistry = amRegistry;
        this.pageService = pageService;
        this.helper = helper;
    }

    @Override
    public void renderTemplate(String location, AsmRendererContext ctx, Writer out) throws AsmRenderingException, IOException {
        AsmTemplateEngineAdapter te = selectTemplateEngineForLocation(location);
        if (te == null) {
            throw new AsmRenderingException("Failed to select template engine for location: " +
                                            location + " assembly: " + ctx.getAsm().getName());
        }
        te.renderTemplate(location, ctx, out);
    }

    @Override
    public boolean isHasRenderableAsmAttribute(Asm asm, AsmRendererContext ctx, String name) {
        boolean ret = asm.isHasAttribute(name);
        if (!ret) {
            CachedPage indexPage = pageService.getIndexPage(ctx.getServletRequest(), false);
            if (indexPage != null && !asm.equals(indexPage.getAsm())) {
                return indexPage.getAsm().isHasAttribute(name);
            }
        }
        return ret;
    }

    @Override
    public void renderAsm(AsmRendererContext ctx, Writer writer) throws AsmRenderingException, IOException {
        Asm asm = ctx.getAsm();
        AsmCore core = asm.getEffectiveCore();
        if (core == null) {
            throw new AsmMissingCoreException(asm.getName());
        }
        ctx.push();
        try {
            if (executeAsmController(asm, ctx) || ctx.getServletResponse().isCommitted()) {
                return; //Assembly handler took full control on response
            }
            AsmTemplateEngineAdapter te = selectTemplateEngineForCore(core);
            if (te == null) {
                throw new AsmRenderingException("Failed to select template engine for assembly " +
                                                "core: " + core + " assembly: " + asm.getName());
            }
            if (log.isTraceEnabled()) {
                log.trace("Selected template engine: {} assembly: {}",
                          te.getClass().getName(), asm.getName());
            }
            te.renderTemplate(core.getLocation(),
                              ctx,
                              (writer != null) ? writer : ctx.getServletResponse().getWriter());
        } finally {
            ctx.pop();
        }
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attributeName,
                                     Map<String, String> options) throws AsmRenderingException {
        if (options == null) {
            options = Collections.EMPTY_MAP;
        }
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attributeName);
        if (attr == null) {
            CachedPage indexPage = pageService.getIndexPage(ctx.getServletRequest(), false);
            if (indexPage != null && !asm.equals(indexPage.getAsm())) {
                return ctx.renderAttribute(indexPage.getAsm(), attributeName, options);
            }
            log.warn("Attribute: '{}' not found in assembly: '{}" + '\'',
                     attributeName, ctx.getRootAsm().getName());
            return null;
        }
        String type = attr.getType();
        if (type == null) {
            type = "*";
        }
        AsmAttributeManager arend = amRegistry.getByType(type);
        if (arend == null) {
            arend = amRegistry.getByType("*");
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
    protected boolean executeAsmController(Asm asm, AsmRendererContext ctx) {
        String controllerClassName = asm.getEffectiveController();
        if (StringUtils.isBlank(controllerClassName)) {
            return false;
        }
        AsmController controller = helper.createControllerInstance(asm, controllerClassName);
        try {
            return controller.execute(ctx);
        } catch (Exception e) {
            throw new AsmRenderingException("Failed to execute assembly handler: '" + controllerClassName +
                                            "' for assembly: " + asm.getName(), e);
        }
    }


    protected AsmTemplateEngineAdapter selectTemplateEngineForLocation(String location) {
        AsmTemplateEngineAdapter defaultTe = null;
        String type = FilenameUtils.getExtension(location);
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
