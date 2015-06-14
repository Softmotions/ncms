package com.softmotions.ncms.asm.am;

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
import javax.servlet.http.HttpServletResponse;
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

    @Inject
    public AsmBreadCrumbsAM(PageService pageService) {
        this.pageService = pageService;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {
        return attr;
    }

    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Tree res = new Tree();
        CachedPage cp = pageService.getCachedPage(ctx.getAsm().getId(), true);
        if (cp == null) {
            return res;
        }
        Map<CachedPage.PATH_TYPE, Object> navpaths = cp.fetchNavPaths();
        Long[] idPaths = (Long[]) navpaths.get(PATH_TYPE.ID);
        String[] labelPaths = (String[]) navpaths.get(PATH_TYPE.LABEL);
        String[] guidPaths = (String[]) navpaths.get(PATH_TYPE.GUID);
        List<Tree> children = res.getChildren();
        CachedPage ip = pageService.getIndexPage(ctx.getServletRequest(), true);
        if (ip != null) {
            Tree c = new Tree();
            c.setId(ip.getId());
            c.setName(ip.getHname());
            c.setLink(pageService.resolvePageLink(ip.getName()));
            children.add(c);
        }
        for (int i = 0, l = idPaths.length; i < l; ++i) {
            CachedPage p = idPaths[i] != null ? pageService.getCachedPage(idPaths[i], true) : null;
            if (p == null || (ip != null && p.getId().equals(ip.getId()))) {
                continue;
            }
            if (ip != null && p.getAsm().isHasAttribute("mainpage")) {
                continue;
            }
            Tree c = new Tree();
            c.setId(idPaths[i]);
            c.setName(labelPaths[i]);
            if (p.isPublished() && i < l - 1) {
                c.setLink(pageService.resolvePageLink(guidPaths[i]));
            }
            children.add(c);
        }
        return res;
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return attr;
    }

    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {

    }
}
