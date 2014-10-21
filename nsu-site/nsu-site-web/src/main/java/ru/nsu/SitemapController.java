package ru.nsu;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class SitemapController implements AsmController {
    private static final Logger log = LoggerFactory.getLogger(SitemapController.class);

    private final AsmDAO adao;
    private final PageService pageService;

    @Inject
    public SitemapController(AsmDAO adao, PageService pageService) {
        this.adao = adao;
        this.pageService = pageService;
    }


    public boolean execute(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();
        HttpServletResponse resp = ctx.getServletResponse();

        String parent = req.getParameter("parent");
        if (!StringUtils.isBlank(parent)) {
            resp.setContentType("text/html");

            try {
                ctx.put("layer", loadPageLayer(Long.valueOf(parent)));
            } catch (NumberFormatException ignored) {
            }

            ctx.getRenderer().renderTemplate("/site/cores/inc/sitemap_level_items.httl", ctx, resp.getWriter());
            return true;
        }

        // depends on current structure
        CachedPage index = pageService.getIndexPage(req);

        List<Asm> layer = loadPageLayer(null);
        if (index != null) {
            long indexId = index.getId();
            Collections.sort(layer, (a1, a2) -> {
                if (indexId == a1.getId()) {
                    return -1;
                } else if (indexId == a2.getId()) {
                    return 1;
                } else {
                    return ObjectUtils.compare(a1.getHname(), a2.getHname());
                }
            });
        }

        ctx.put("layer", layer);

        return false;
    }

    private List<Asm> loadPageLayer(Long parent) {
        Collection<Asm> asms = adao.selectPageLayer(parent);
        List<Asm> result = new ArrayList<>(asms.size());
        asms.stream().filter(Asm::isPublished).forEach(result::add);
        return result;
    }
}
