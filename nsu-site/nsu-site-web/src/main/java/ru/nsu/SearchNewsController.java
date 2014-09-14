package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class SearchNewsController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(SearchNewsController.class);

    private static final int DEFAULT_MAX_RESULTS = 20;

    private final AsmDAO adao;

    private final SolrServer solr;

    @Inject
    public SearchNewsController(AsmDAO adao, SolrServer solr) {
        this.adao = adao;
        this.solr = solr;
    }

    public boolean execute(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();

        int offset = 0;
        int limit = DEFAULT_MAX_RESULTS;

        String offsetStr = req.getParameter("spc.start");
        try {
            offset = !StringUtils.isBlank(offsetStr) ? Integer.parseInt(offsetStr) : offset;
        } catch (NumberFormatException ignored) {
        }

        String limitStr = req.getParameter("spc.limit");
        try {
            limit = !StringUtils.isBlank(limitStr) ? Integer.parseInt(limitStr) : limit;
        } catch (NumberFormatException ignored) {
        }

        ModifiableSolrParams params = new ModifiableSolrParams();

        // фильтр на только новости
        // TODO: configure types for search?
        params.add(CommonParams.FQ, "+type:news* +published:true");
        // поисковая строка. если поисковая строка пустая - используем поиска по всем, иначе на всякий случай эскейпим спец символы
        String text = req.getParameter("spc.text");
        ctx.put("search_query", text);
        text = StringUtils.isBlank(text) ? "*" : text; //QueryParser.escape(text);
        params.add(CommonParams.Q, text);

        List<String> timeScopes = Arrays.asList("all", "year", "half-year", "month");

        String timeScope = req.getParameter("spc.scope");
        timeScope = StringUtils.isBlank(timeScope) || !timeScopes.contains(timeScope) ? timeScopes.get(0) : timeScope;
        ctx.put("search_scope", timeScope);
        // TODO use time scope


        // нам нужны только поля:
        //  id - идентификатор
        //  score - "баллы" поиска, для сортировки по релевантности
        params.add(CommonParams.FL, "id,score");
        // выставляем сортировку по релевантности
        params.add(CommonParams.SORT, "score desc");
        // пэйджинг
        params.add(CommonParams.START, String.valueOf(offset));
        params.add(CommonParams.ROWS, String.valueOf(limit));

        QueryResponse queryResponse = solr.query(params);
        SolrDocumentList results = queryResponse.getResults();

        // TODO: переписать на селект типа: id IN (:ids)
        Collection<Asm> asms = new ArrayList<>(results.size());
        for (SolrDocument document : results) {
            asms.add(adao.asmSelectById(Long.valueOf(String.valueOf(document.getFieldValue("id")))));
        }

        ctx.put("search_result", asms);

        return false;
    }
}