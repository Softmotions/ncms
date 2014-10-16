package ru.nsu;

import ru.nsu.events.EventsRemember;
import com.softmotions.commons.date.DateHelper;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.jaxrs.NcmsMessageException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class EventsMainController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(EventsMainController.class);

    private final NewsDirectoryController ndc;

    private final AsmDAO adao;

    private final ObjectMapper mapper;

    private final NcmsMessages messages;

    private final EventsRemember remember;

    @Inject
    public EventsMainController(NewsDirectoryController ndc, AsmDAO adao, ObjectMapper mapper, NcmsMessages messages, EventsRemember remember) {
        this.ndc = ndc;
        this.adao = adao;
        this.mapper = mapper;
        this.messages = messages;
        this.remember = remember;
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();
        String action = StringUtils.trimToEmpty(req.getParameter("action"));

        switch (action) {
            case "remember":
                executeRemember(ctx);
                return true;

            default:
                return executeDefault(ctx);
        }
    }

    private void executeRemember(AsmRendererContext ctx) throws IOException {
        HttpServletRequest req = ctx.getServletRequest();
        HttpServletResponse resp = ctx.getServletResponse();

        ObjectNode result = mapper.createObjectNode();
        try {
            Long eventId = Long.valueOf(req.getParameter("eventId"));
            String contact = StringUtils.trimToEmpty(req.getParameter("contact")).toLowerCase();

            remember.saveRememeber(eventId, contact);

            result.put("success", true);
        } catch (NcmsMessageException e) {
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("message", messages.get("ncms.events.remember.unexpected.error"));
            log.error("", e);
        } finally {
            if (!result.has("success")) {
                result.put("success", false);
            }

            resp.setContentType("application/json");
            resp.getWriter().write(result.toString());
        }
    }

    private boolean executeDefault(AsmRendererContext ctx) throws Exception {
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
