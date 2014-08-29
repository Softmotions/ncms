package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.Tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.softmotions.ncms.asm.CachedPage.PATH_TYPE;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmBreadCrumbsAM implements AsmAttributeManager {

    public static final String[] TYPES = new String[]{"breadcrumbs"};

    private final PageService pageService;

    private final NcmsConfiguration cfg;

    @Inject
    public AsmBreadCrumbsAM(PageService pageService, NcmsConfiguration cfg) {
        this.pageService = pageService;
        this.cfg = cfg;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(Asm template, AsmAttribute tmplAttr, AsmAttribute attr) {
        return attr;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        CachedPage cp = pageService.getCachedPage(ctx.getAsm().getId(), true);
        Map<CachedPage.PATH_TYPE, Object> navpaths = cp.fetchNavPaths();
        Long[] idPaths = (Long[]) navpaths.get(PATH_TYPE.ID);
        String[] labelPaths = (String[]) navpaths.get(PATH_TYPE.LABEL);
        Tree res = new Tree();
        List<Tree> children = res.getChildren();
        CachedPage ip = pageService.getIndexPage(ctx.getServletRequest());
        if (ip != null) {
            Tree c = new Tree();
            c.setId(ip.getAsm().getId());
            c.setName(ip.getAsm().getHname());
            c.setLink(cfg.getAsmLink(ip.getAsm().getId()));
            children.add(c);
        }
        for (int i = 0, l = idPaths.length; i < l; ++i) {
            CachedPage p = pageService.getCachedPage(idPaths[i], true);
            Tree c = new Tree();
            c.setId(idPaths[i]);
            c.setName(labelPaths[i]);
            if (p != null && p.getAsm().isPublished() && i < l - 1) {
                c.setLink(cfg.getAsmLink(idPaths[i]));
            }
            children.add(c);
        }
        return res;
    }

    public AsmAttribute applyAttributeOptions(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttribute attr, JsonNode val, HttpServletRequest req) {
        return attr;
    }

    public void attributePersisted(AsmAttribute attr, JsonNode val, HttpServletRequest req) {

    }
}
