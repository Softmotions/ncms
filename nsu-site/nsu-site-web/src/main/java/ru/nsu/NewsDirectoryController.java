package ru.nsu;

import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */

@Singleton
public class NewsDirectoryController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(NewsDirectoryController.class);

    public static final int DEFAUL_NEWS_LIMIT = 8;

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();
        HttpServletResponse resp = ctx.getServletResponse();

        int skip = 0;
        String skipStr = req.getParameter("news.skip");
        try {
            skip = !StringUtils.isBlank(skipStr) ? Integer.parseInt(skipStr) : skip;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("news_skip", skip);

        int limit = DEFAUL_NEWS_LIMIT;
        String limitStr = req.getParameter("news.limit");
        try {
            limit = !StringUtils.isBlank(limitStr) ? Integer.parseInt(limitStr) : limit;
        } catch (NumberFormatException ignored) {
        }
        if (limit > Constants.MAX_TOTAL_ITEMS_LIMIT) {
            limit = Constants.MAX_TOTAL_ITEMS_LIMIT;
        }
        ctx.put("news_limit", limit);

        if (req.getParameter("rss") != null) {
            resp.setContentType("application/xml; charset=UTF-8");
            ctx.getRenderer().renderTemplate("/site/cores/inc/news_rss.httl", ctx, resp.getWriter());
            return true;
        } else {
            String action = req.getParameter("news.action");
            if ("fetchMore".equals(action)) {
                AsmAttribute results = ctx.getAsm().getEffectiveAttribute("news_list");
                if (results != null) {
                    ctx.getRenderer().renderTemplate(results.getEffectiveValue(), ctx, resp.getWriter());
                } else {
                    log.error("Atribute 'news_list' is not found");
                }
                return true;
            }
        }
        return false;
    }
}
