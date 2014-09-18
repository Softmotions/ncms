package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class SearchController implements AsmController {

    protected static final int DEFAULT_MAX_RESULTS = 20;

    protected final Logger log;
    protected final AsmDAO adao;

    protected final SolrServer solr;

    @Inject
    public SearchController(AsmDAO adao, SolrServer solr) {
        this.log = LoggerFactory.getLogger(getClass());
        this.adao = adao;
        this.solr = solr;
    }

    public boolean execute(AsmRendererContext ctx) throws Exception {
        prepare(ctx);
        doSearch(ctx);

        HttpServletRequest req = ctx.getServletRequest();
        HttpServletResponse resp = ctx.getServletResponse();
        String action = req.getParameter("spc.action");
        if ("search".equals(action)) {
            resp.setContentType("text/html");
            AsmAttribute results = ctx.getAsm().getEffectiveAttribute("results");
            ctx.getRenderer().renderTemplate(results.getEffectiveValue(), ctx, resp.getWriter());

            return true;
        }
        return false;
    }

    protected void prepare(AsmRendererContext ctx) {
        HttpServletRequest req = ctx.getServletRequest();

        int offset = 0;
        int limit = DEFAULT_MAX_RESULTS;

        String offsetStr = req.getParameter("spc.start");
        try {
            offset = !StringUtils.isBlank(offsetStr) ? Integer.parseInt(offsetStr) : offset;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("search_start", offset);

        String limitStr = req.getParameter("spc.limit");
        try {
            limit = !StringUtils.isBlank(limitStr) ? Integer.parseInt(limitStr) : limit;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("search_limit", limit);

        String text = req.getParameter("spc.text");
        ctx.put("search_query", text);
    }

    protected void doSearch(AsmRendererContext ctx) throws Exception {
        ModifiableSolrParams params = new ModifiableSolrParams();

        // поисковая строка. если поисковая строка пустая - используем поиска по всем, иначе на всякий случай эскейпим спец символы
        String text = (String) ctx.get("search_query");
        text = StringUtils.isBlank(text) ? "*" : QueryParser.escape(text);
        params.add(CommonParams.Q, text);

        // инициализируем фильтр
        params.add(CommonParams.FQ, buildFilterQuery(ctx));

        // пэйджинг
        int offset = ctx.get("search_start") != null ? (int) ctx.get("search_start") : 0;
        params.add(CommonParams.START, String.valueOf(offset));
        int limit = ctx.get("search_limit") != null ? (int) ctx.get("search_limit") : DEFAULT_MAX_RESULTS;
        params.add(CommonParams.ROWS, String.valueOf(limit));

        // нам нужны только поля:
        //  id - идентификатор
        //  score - "баллы" поиска, для сортировки по релевантности
        //  cdate - дата создания
        params.add(CommonParams.FL, "id,score,cdate");
        // выставляем сортировку по релевантности и дате создания
        params.add(CommonParams.SORT, "score desc, cdate desc");

        QueryResponse queryResponse = solr.query(params);
        SolrDocumentList results = queryResponse.getResults();

        Collection<Asm> asms = new ArrayList<>(results.size());
        for (SolrDocument document : results) {
            asms.add(adao.asmSelectById(Long.valueOf(String.valueOf(document.getFieldValue("id")))));
        }

        ctx.put("search_result", asms);
    }

    protected String buildFilterQuery(AsmRendererContext ctx) throws Exception {
        // ищем только опубликованное
        return "+published:true ";
    }
}
