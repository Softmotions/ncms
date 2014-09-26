package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main page {@link com.softmotions.ncms.asm.render.AsmController}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class MainPageController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(MainPageController.class);

    public static final String[] DEFAULT_ATTRS_INCLUDE = {"annotation",
                                                          "icon",
                                                          "category",
                                                          "subcategory",
                                                          "event_date"};

    private final AsmDAO adao;

    private final ObjectMapper mapper;

    private final SubnodeConfiguration mpCfg;

    @Inject
    public MainPageController(AsmDAO adao,
                              ObjectMapper mapper,
                              NcmsEnvironment env) {
        this.adao = adao;
        this.mapper = mapper;
        this.mpCfg = env.xcfg().configurationAt("content.mainpage");
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();
        HttpServletResponse resp = ctx.getServletResponse();
        String action = req.getParameter("mpc.action");
        if ("fetchMore".equals(action)) {
            String type = StringUtils.trimToEmpty(req.getParameter("mpc.fetch.type"));
            String templateLocation = "/site/cores/inc/index_news_" + type + ".httl";
            switch (type) {
                case "a":
                    addNewsA(ctx);
                    break;
                case "b":
                    addNewsB(ctx);
                    break;
                case "c":
                    addNewsC(ctx);
                    break;
                default:
                    return true;
            }
            resp.setContentType("text/html");
            ctx.getRenderer().renderTemplate(templateLocation, ctx, resp.getWriter());
            return true;
        }
        addNewsA(ctx);
        addNewsB(ctx);
        addNewsC(ctx);
        return false;
    }

    private void addNewsC(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();
        String val = req.getParameter("mpc.c.skip");
        Integer skip = (val != null) ? Integer.parseInt(val) : null;

        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withAttributes(DEFAULT_ATTRS_INCLUDE);
        crit.withPublished(true);
        crit.withTypeLike("news.page");
        String[] templates = mpCfg.getStringArray("news.c[@templates]");
        if (templates == null) {
            templates = new String[]{"faculty_news"};
        }
        crit.withTemplates(templates);

        if (skip != null) {
            crit.skip(skip);
        }
        crit.limit(mpCfg.getInt("news.c[@max]", Constants.MAX_TOTAL_ITEMS_LIMIT));
        crit.onAsm().orderBy("ordinal").desc();

        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("news_c", news);
    }

    private void addNewsB(AsmRendererContext ctx) throws Exception {
        Asm asm = ctx.getAsm();
        HttpServletRequest req = ctx.getServletRequest();

        String val = req.getParameter("mpc.b.skip");
        Integer skip = (val != null) ? Integer.parseInt(val) : null;

        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withAttributes(DEFAULT_ATTRS_INCLUDE);
        crit.withPublished(true);
        crit.withNavParentId(asm.getId());
        crit.withTypeLike("news.page");
        String[] templates = mpCfg.getStringArray("news.b[@templates]");
        if (templates == null) {
            templates = new String[]{"index_announce"};
        }
        crit.withTemplates(templates);

        if (skip != null) {
            crit.skip(skip);
        }
        crit.limit(mpCfg.getInt("news.b[@max]", Constants.MAX_TOTAL_ITEMS_LIMIT));
        crit.onAsm().orderBy("ordinal").desc();

        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("news_b", news);
    }

    private void addNewsA(AsmRendererContext ctx) throws Exception {
        Asm asm = ctx.getAsm();
        HttpServletRequest req = ctx.getServletRequest();

        String val = req.getParameter("mpc.a.skip");
        int skip = (val != null) ? Integer.parseInt(val) : 0;
        String activeCategory = StringUtils.trimToEmpty(req.getParameter("mpc.a.subType"));

        String defaultCategory = null;
        List<Pair<String, String>> newsCategories = new ArrayList<>();
        Map<String, Configuration> ncConfigs = new HashMap<>();
        for (HierarchicalConfiguration aCfg : mpCfg.configurationsAt("news.a")) {
            String type = aCfg.getString("[@type]", "");
            String title = aCfg.getString("[@title]", "");

            newsCategories.add(new Pair<>(type, title));
            ncConfigs.put(type, aCfg);
            if (defaultCategory == null) {
                defaultCategory = type;
            }
        }
        if (activeCategory == null || !ncConfigs.containsKey(activeCategory)) {
            activeCategory = defaultCategory;
        }

        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withAttributes(DEFAULT_ATTRS_INCLUDE);
        crit.withPublished(true);
        crit.withNavParentId(asm.getId());
        crit.withTypeLike("news.page");

        Configuration aCfg = activeCategory != null ? ncConfigs.get(activeCategory) : null;
        String[] templates = aCfg != null ? aCfg.getStringArray("[@templates]") : null;
        if (templates == null) {
            templates = new String[]{"index_news", "index_reportage", "index_interview"};
        }
        crit.withTemplates(templates);

        crit.skip(skip);

        int limit = Constants.MAX_TOTAL_ITEMS_LIMIT;
        if (aCfg != null) {
            limit = aCfg.getInt("[@max]", limit);
        }
        crit.limit(limit);
        crit.onAsm().orderBy("ordinal").desc();

        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("activeCategory", activeCategory);
        ctx.put("newsCategories", newsCategories);
        ctx.put("news_a", news);
    }
}
