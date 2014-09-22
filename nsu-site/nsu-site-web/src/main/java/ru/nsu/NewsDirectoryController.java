package ru.nsu;

import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.guice.transactional.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class NewsDirectoryController implements AsmController {

    private static final int MAX_TOTAL_NEWS_LIMIT = 8;

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

        int limit = MAX_TOTAL_NEWS_LIMIT;
        String limitStr = req.getParameter("news.limit");
        try {
            limit = !StringUtils.isBlank(limitStr) ? Integer.parseInt(limitStr) : limit;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("news_limit", limit);

        String action = req.getParameter("news.action");
        if ("fetchMore".equals(action)) {
            AsmAttribute results = ctx.getAsm().getEffectiveAttribute("news_list");
            ctx.getRenderer().renderTemplate(results.getEffectiveValue(), ctx, resp.getWriter());
            return true;
        }

        return false;
    }
}
