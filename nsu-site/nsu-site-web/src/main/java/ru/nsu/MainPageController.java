package ru.nsu;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.Collection;

/**
 * Main page {@link com.softmotions.ncms.asm.render.AsmController}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MainPageController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(MainPageController.class);

    private final AsmDAO dao;

    private final PageService pageService;

    private final ObjectMapper mapper;

    @Inject
    public MainPageController(AsmDAO dao,
                              PageService pageService,
                              ObjectMapper mapper) {
        this.dao = dao;
        this.pageService = pageService;
        this.mapper = mapper;
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        Asm asm = ctx.getAsm();
        HttpSession sess = ctx.getServletRequest().getSession();

        String[] newsCategories = asm.getEffectiveAttributeAsStringArray("news_categories", mapper);
        Integer activeCategoryId = (Integer) sess.getAttribute("MainPageController.activeCategoryId");
        if (newsCategories.length > 0 &&
            (activeCategoryId == null || activeCategoryId.intValue() >= newsCategories.length)) {
            activeCategoryId = 0;
            sess.setAttribute("MainPageController.activeCategoryId", activeCategoryId);
        }

        String activeCategory =
                (activeCategoryId != null && activeCategoryId.intValue() < newsCategories.length) ?
                newsCategories[activeCategoryId] : null;

        AsmDAO.PageCriteria crit = dao.newPageCriteria();
        crit.withPublished(true);
        crit.withNavParentId(asm.getId());
        crit.withTypeLike("news.page");
        crit.withAttributeLike("annotation", "%");
        crit.withAttributeLike("category", (activeCategory != null) ? activeCategory : "%");
        crit.withAttributeLike("subcategory", "%");
        crit.withAttributeLike("icon", "%");
        crit.onAsm().orderBy("ordinal").desc();

        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("activeCategory", activeCategory);
        ctx.put("newsCategories", newsCategories);
        ctx.put("news", news);

        return false;
    }
}
