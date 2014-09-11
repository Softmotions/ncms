package ru.nsu;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Main page {@link com.softmotions.ncms.asm.render.AsmController}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MainPageController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(MainPageController.class);

    private static final int MAX_TOTAL_NEWS_LIMIT = 1000;

    private static final String[] DEFAULT_ATTRS_INCLUDE = {"annotation",
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
                              NcmsConfiguration cfg) {
        this.adao = adao;
        this.mapper = mapper;
        this.mpCfg = cfg.impl().configurationAt("content.mainpage");
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
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
        crit.attributesInclude(DEFAULT_ATTRS_INCLUDE);
        crit.withPublished(true);
        crit.withTypeLike("news.page");
        crit.withTemplates(mpCfg.getString("news.c[@template]", "faculty_news"));

        if (skip != null) {
            crit.skip(skip);
        }
        crit.limit(mpCfg.getInt("news.c[@max]", MAX_TOTAL_NEWS_LIMIT));
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
        crit.attributesInclude(DEFAULT_ATTRS_INCLUDE);
        crit.withPublished(true);
        crit.withNavParentId(asm.getId());
        crit.withTypeLike("news.page");
        crit.withTemplates(mpCfg.getString("news.b[@template]", "index_announce"));

        if (skip != null) {
            crit.skip(skip);
        }
        crit.limit(mpCfg.getInt("news.b[@max]", MAX_TOTAL_NEWS_LIMIT));
        crit.onAsm().orderBy("ordinal").desc();

        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("news_b", news);
    }

    private void addNewsA(AsmRendererContext ctx) throws Exception {
        Asm asm = ctx.getAsm();
        HttpServletRequest req = ctx.getServletRequest();

        String val = req.getParameter("mpc.a.skip");
        Integer skip = (val != null) ? Integer.parseInt(val) : null;
        String activeCategory = req.getParameter("mpc.a.ac");

        String[] newsCategories = asm.getEffectiveAttributeAsStringArray("news_categories", mapper);
        if (activeCategory == null && newsCategories.length > 0) {
            activeCategory = newsCategories[0];
        }

        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.attributesInclude(DEFAULT_ATTRS_INCLUDE);
        crit.withPublished(true);
        crit.withNavParentId(asm.getId());
        crit.withTypeLike("news.page");
        crit.withTemplates(mpCfg.getString("news.a[@template]", "index_news"));

        if (activeCategory != null) {
            crit.withAttributeLike("category", activeCategory);
        }
        if (skip != null) {
            crit.skip(skip);
        }
        crit.limit(mpCfg.getInt("news.a[@max]", MAX_TOTAL_NEWS_LIMIT));
        crit.onAsm().orderBy("ordinal").desc();

        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("activeCategory", activeCategory);
        ctx.put("newsCategories", newsCategories);
        ctx.put("news_a", news);
    }
}
