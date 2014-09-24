package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.guice.transactional.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main page for news {@link com.softmotions.ncms.asm.render.AsmController}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NewsMainPageController implements AsmController {

    private static final int MAX_TOTAL_NEWS_LIMIT = 16;

    private final AsmDAO adao;

    private final SubnodeConfiguration npCfg;

    @Inject
    public NewsMainPageController(AsmDAO adao, NcmsEnvironment env) {
        this.adao = adao;
        this.npCfg = env.xcfg().configurationAt("content.newsmain");
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();
        HttpServletResponse resp = ctx.getServletResponse();

        String action = StringUtils.trimToEmpty(req.getParameter("mnc.action"));
        String partialResource = null;
        switch (action) {
            case "fetchMoreNews" :
                addNews(ctx);
                partialResource = "/site/cores/inc/news_main_news.httl";
                break;

            default:
                partialResource = null;
        }

        if (partialResource != null) {
            resp.setContentType("text/html");
            ctx.getRenderer().renderTemplate(partialResource, ctx, resp.getWriter());
            return true;
        }

        addMainEvents(ctx);
        addNews(ctx);
        return false;
    }

    private void addMainEvents(AsmRendererContext ctx) throws Exception {
        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withTypeLike("news.page");
        crit.withTemplates("index_news",
                           "index_interview",
                           "index_reportage",
                           "index_announce",
                           "index_orders");
        crit.withAttributeLike("mainevent", "true");
        crit.limit(3);
        crit.onAsm().orderBy("ordinal").desc();
        Collection<Asm> events = crit.selectAsAsms();
        ctx.put("events", events);
    }

    private void addNews(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();

        String activeType = StringUtils.trimToEmpty(req.getParameter("mnc.news.type"));

        String defaultType = null;
        Collection<Pair<String, String>> ncList = new ArrayList<>();
        Map<String, Configuration> ncConfigs = new HashMap<>();
        for (HierarchicalConfiguration nCfg : npCfg.configurationsAt("news.type")) {
            String type = nCfg.getString("[@type]", "");
            String title = nCfg.getString("[@title]", "");

            ncList.add(new Pair<>(type, title));
            ncConfigs.put(type, nCfg);
            if (defaultType == null) {
                defaultType = type;
            }
        }
        if (activeType == null || !ncConfigs.containsKey(activeType)) {
            activeType = defaultType;
        }

        int skip = 0;
        String skipStr = req.getParameter("mnc.news.skip");
        try {
            skip = !StringUtils.isBlank(skipStr) ? Integer.parseInt(skipStr) : skip;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("news_skip", skip);

        int limit = MAX_TOTAL_NEWS_LIMIT;
        String limitStr = req.getParameter("mnc.news.limit");
        try {
            limit = !StringUtils.isBlank(limitStr) ? Integer.parseInt(limitStr) : limit;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("news_limit", limit);

        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withTypeLike("news.page");

        Configuration nCfg = activeType != null ? ncConfigs.get(activeType) : null;
        String[] templates = nCfg != null ? nCfg.getStringArray("[@templates]") : null;
        if (templates == null) {
            templates = new String[]{"index_news", "index_interview", "index_reportage", "faculty_news", "index_orders"};
        }
        crit.withTemplates(templates);

        crit.skip(skip);
        crit.limit(limit);
        crit.onAsm().orderBy("ordinal").desc();
        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("news", news);
        ctx.put("news_active_type", activeType);
        ctx.put("news_categories", ncList);
    }
}
