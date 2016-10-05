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
        String ref = ctx.getEnvironment().getNcmsAdminRoot() + "/resource/ncms/script/medium-editor.min.js";
        return "<script src=\"" + ref + "\"/>";
    }

    /**
     * Return `true` if content of visual block identified by `blockName` exists
     *
     * @param blockName Visual block name.
     */
    public static boolean ncmsVEBlockExists(String blockName) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        AsmAttribute attr = ctx.getAsm().getUniqueEffectiveAttributeByType(AsmVisualEditorAM.TYPE);
        if (attr == null) {
            return false;
        }
        AsmVisualEditorAM amreg = (AsmVisualEditorAM) ctx.getPageService()
                                                         .getAsmAttributeManagersRegistry()
                                                         .getByType(AsmVisualEditorAM.TYPE);
        if (amreg == null) {
            return false;
        }
        // todo
        return false;
    }

    @Nullable
    public static String ncmsVEBlock(String blockName) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        AsmAttribute attr = ctx.getAsm()
                               .getUniqueEffectiveAttributeByType(AsmVisualEditorAM.TYPE);

        return null;
    }
}
