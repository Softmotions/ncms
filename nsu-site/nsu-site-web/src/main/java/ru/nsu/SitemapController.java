package ru.nsu;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class SitemapController implements AsmController {
    private static final Logger log = LoggerFactory.getLogger(SitemapController.class);

    private final AsmDAO adao;

    @Inject

    public SitemapController(AsmDAO adao) {
        this.adao = adao;
    }


    public boolean execute(AsmRendererContext ctx) throws Exception {
        // TODO:

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

        ctx.put("layer", loadPageLayer(null));

        return false;
    }

    private Collection<Asm> loadPageLayer(Long parent) {
        Collection<Asm> asms = adao.selectPageLayer(parent);
        Collection<Asm> result = new ArrayList<>(asms.size());
        asms.stream().filter((asm) -> asm.isPublished() || "page.folder".equals(asm.getType())).forEach(result::add);
        return result;
    }
}
