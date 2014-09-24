package ru.nsu;

import com.softmotions.commons.date.DateHelper;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Inject;

import org.mybatis.guice.transactional.Transactional;

import java.util.Date;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class EventsMainController implements AsmController {

    private final NewsDirectoryController ndc;

    private final AsmDAO adao;

    @Inject
    public EventsMainController(NewsDirectoryController ndc, AsmDAO adao) {
        this.ndc = ndc;
        this.adao = adao;
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withTemplates("index_announce", "faculty_announce");
        crit.withPublished(true);
        crit.withEdateGTE(DateHelper.trunkDayDate(new Date()));
        crit.withAttributes("annotation",
                            "icon",
                            "bigicon",
                            "category",
                            "subcategory",
                            "event_date");
        crit.onAsm().orderBy("edate").asc();
        ctx.put("criteria", crit);
        return ndc.execute(ctx);
    }
}
