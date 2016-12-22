package com.softmotions.ncms.vedit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.am.AsmVisualEditorAM;
import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * Visual editor HTTL helper methods.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class HttlVisualEditorMethods {

    private HttlVisualEditorMethods() {
    }

    /**
     * Visual editor meta attributes on `<html>` element.
     */
    @Nullable
    public static String ncmsDocumentVEMeta() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        if (ctx.getUserData("ncmsDocumentVEMeta.applied") != null
            || !ctx.getPageService().getPageSecurityService().isPreviewPageRequest(ctx.getServletRequest())) {
            return null;
        }
        ctx.setUserData("ncmsDocumentVEMeta.applied", Boolean.TRUE);
        return " data-ncms-root=\"" + AsmRendererContext.getSafe().getEnvironment().getAppRoot() + "\"";
    }

    /**
     * Visual editor css styles.
     */
    @Nullable
    public static String ncmsVEStyles() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        if (ctx.getUserData("ncmsVEStyles.applied") != null
            || !ctx.getPageService().getPageSecurityService().isPreviewPageRequest(ctx.getServletRequest())) {
            return null;
        }
        ctx.setUserData("ncmsVEStyles.applied", Boolean.TRUE);
        String ref = ctx.getEnvironment().getNcmsAdminRoot() + "/resource/ncms/css/medium-editor.css";
        return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + ref + "\"/>";
    }

    /**
     * Visual editor `<script>` elements.
     */
    @Nullable
    public static String ncmsVEScripts() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        if (ctx.getUserData("ncmsVEScripts.applied") != null
            || !ctx.getPageService().getPageSecurityService().isPreviewPageRequest(ctx.getServletRequest())) {
            return null;
        }
        ctx.setUserData("ncmsVEScripts.applied", Boolean.TRUE);
        String ref = ctx.getEnvironment().getNcmsAdminRoot() + "/resource/ncms/script/medium-editor.js";
        String ref2 = ctx.getEnvironment().getNcmsAdminRoot() + "/resource/ncms/script/ncms-preview.js";
        String ret = "<script type=\"text/javascript\" src=\"" + ref + "\"></script>\n";
        ret += "<script type=\"text/javascript\" src=\"" + ref2 + "\"></script>\n";
        return ret;
    }

    @Nullable
    public static String ncmsVEBlock(String sectionName) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        AsmAttribute attr = ctx.getRootContext().getAsm()
                               .getUniqueEffectiveAttributeByType(AsmVisualEditorAM.TYPE);
        AsmVisualEditorAM am = ctx.getPageService()
                                  .getAsmAttributeManagersRegistry()
                                  .getByType(AsmVisualEditorAM.TYPE);
        if (attr == null || am == null) {
            return null;
        }
        return am.getSection(ctx, attr, sectionName);
    }

    @Nonnull
    public static String ncmsVEBlockId(String sectionName) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        return ctx.getRootContext().getAsm().getId() + ":" + sectionName;
    }

    /**
     * Return `true` if content of visual block identified by `sectionName` exists
     *
     * @param sectionName Visual block name.
     */
    public static boolean ncmsVEBlockExists(String sectionName) {
        return ncmsVEBlock(sectionName) != null;
    }
}
