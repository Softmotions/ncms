package com.softmotions.ncms.vedit;

import javax.annotation.Nullable;

import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.am.AsmVisualEditorAM;
import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttlVisualEditorMethods {

    private HttlVisualEditorMethods() {
    }

    /**
     * Visual editor css styles.
     */
    public static String ncmsVEStyles() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        if (!ctx.getPageService().getPageSecurityService().isPreviewPageRequest(ctx.getServletRequest())) {
            return "";
        }
        String ref = ctx.getEnvironment().getNcmsAdminRoot() + "/resource/ncms/css/medium-editor.css";
        return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + ref + "\"/>";
    }

    /**
     * Visual editor `<script>` elements.
     */
    public static String ncmsVEScripts() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        if (!ctx.getPageService().getPageSecurityService().isPreviewPageRequest(ctx.getServletRequest())) {
            return "";
        }
        String ref = ctx.getEnvironment().getNcmsAdminRoot() + "/resource/ncms/script/medium-editor.js";
        String ref2 = ctx.getEnvironment().getNcmsAdminRoot() + "/resource/ncms/script/ncms-preview.js";
        String ret = "<script type=\"text/javascript\" src=\"" + ref + "\"></script>\n";
        ret += "<script type=\"text/javascript\" src=\"" + ref2 + "\"></script>\n";
        return ret;
    }

    @Nullable
    public static String ncmsVEBlock(String sectionName) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        AsmAttribute attr = ctx.getAsm()
                               .getUniqueEffectiveAttributeByType(AsmVisualEditorAM.TYPE);
        AsmVisualEditorAM am = ctx.getPageService()
                                  .getAsmAttributeManagersRegistry()
                                  .getByType(AsmVisualEditorAM.TYPE);
        if (attr == null || am == null) {
            return null;
        }
        return am.getSection(ctx, attr, sectionName);
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
