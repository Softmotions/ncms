package ru.nsu;

import com.softmotions.commons.date.DateHelper;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Inject;

import org.mybatis.guice.transactional.Transactional;

import javax.servlet.http.HttpServletRequest;
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
        HttpServletRequest req = ctx.getServletRequest();
        boolean past = req.getParameter("past") != null;
        ctx.put("events_past", past);

        AsmDAO.PageCriteria crit = adao.newPageCriteria();

        crit.withTemplates("index_announce", "faculty_announce");
        crit.withPublished(true);
        crit.withAttributes("annotation",
                            "icon",
                            "bigicon",
                            "subcategory",
                            "event_date");
        if (past) {
            crit.withEdateLTE(new Date(DateHelper.trunkDayDate(new Date()).getTime() - 1));
            crit.onAsm().orderBy("edate").desc();
        } else {
            crit.withEdateGTE(DateHelper.trunkDayDate(new Date()));
            crit.onAsm().orderBy("edate").asc();
        }

        ctx.put("criteria", crit);
        return ndc.execute(ctx);
    }
}
