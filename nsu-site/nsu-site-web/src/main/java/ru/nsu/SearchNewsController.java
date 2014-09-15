package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.mhttl.SelectNode;

import com.google.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class SearchNewsController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(SearchNewsController.class);

    private static final int DEFAULT_MAX_RESULTS = 20;

    private static final Map<String, Callable<Pair<Date, Date>>> TIME_SCOPES;
    private static final String DEFAULT_TIME_SCOPE = "all";

    private final AsmDAO adao;

    private final SolrServer solr;

    static {
        TIME_SCOPES = new HashMap<>();
        TIME_SCOPES.put(DEFAULT_TIME_SCOPE, new Callable<Pair<Date, Date>>() {
            public Pair<Date, Date> call() throws Exception {
                return null;
            }
        });
        // TODO: configure?
        TIME_SCOPES.put("year", new Callable<Pair<Date, Date>>() {
            public Pair<Date, Date> call() throws Exception {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, -1);
                return new Pair<>(cal.getTime(), null);
            }
        });
        TIME_SCOPES.put("half-year", new Callable<Pair<Date, Date>>() {
            public Pair<Date, Date> call() throws Exception {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -6);
                return new Pair<>(cal.getTime(), null);
            }
        });
        TIME_SCOPES.put("month", new Callable<Pair<Date, Date>>() {
            public Pair<Date, Date> call() throws Exception {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1);
                return new Pair<>(cal.getTime(), null);
            }
        });
    }

    @Inject
    public SearchNewsController(AsmDAO adao, SolrServer solr) {
        this.adao = adao;
        this.solr = solr;
    }

    public boolean execute(AsmRendererContext ctx) throws Exception {
        prepare(ctx);

        HttpServletRequest req = ctx.getServletRequest();

        String action = req.getParameter("spc.action");
        if ("search".equals(action)) {
            ctx.put("results_only", true);
        }

        doSearch(ctx);

        return false;
    }

    private void prepare(AsmRendererContext ctx) {
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

        String timeScope = req.getParameter("spc.scope");
        timeScope = StringUtils.isBlank(timeScope) || !TIME_SCOPES.containsKey(timeScope) ? DEFAULT_TIME_SCOPE : timeScope;
        ctx.put("search_scope", timeScope);

        Collection<Pair<String, Boolean>> categories = new ArrayList<>();
        Collection<String> selectedCategories = new ArrayList<>();
        Object cObj = ctx.getRenderer().renderAsmAttribute(ctx, "categories", Collections.EMPTY_MAP);
        if (cObj instanceof Collection) {
            String[] categoryNames = req.getParameterValues("spc.category");
            for (SelectNode category : (Iterable<SelectNode>) cObj) {
                boolean selected = false;
                if (categoryNames != null) {
                    for (String categoryName : categoryNames) {
                        if (category.getValue().equals(categoryName)) {
                            selected = true;
                            break;
                        }
                    }
                }
                categories.add(new Pair<>(category.getValue(), selected));
                if (selected) {
                    selectedCategories.add(category.getValue());
                }
            }
        }
        ctx.put("search_categories", categories);
        ctx.put("search_categories_selected", selectedCategories);
    }

    private void doSearch(AsmRendererContext ctx) throws Exception {
        ModifiableSolrParams params = new ModifiableSolrParams();

        // поисковая строка. если поисковая строка пустая - используем поиска по всем, иначе на всякий случай эскейпим спец символы
        String text = (String) ctx.get("search_query");
        text = StringUtils.isBlank(text) ? "*" : QueryParser.escape(text);
        params.add(CommonParams.Q, text);

        String timeScopeFQ = "";
        String timeScopeName = (String) ctx.get("search_scope");
        if (!StringUtils.isBlank(timeScopeName) && TIME_SCOPES.containsKey(timeScopeName)) {
            Callable<Pair<Date, Date>> tsc = TIME_SCOPES.get(timeScopeName);
            Pair<Date, Date> timeScope = tsc != null ? tsc.call() : null;
            if (timeScope != null && (timeScope.getOne() != null || timeScope.getTwo() != null)) {
                timeScopeFQ =
                        " +cdate:" +
                        "[" +
                        (timeScope.getOne() == null ? "*" : String.valueOf(timeScope.getOne().getTime())) +
                        " TO " +
                        (timeScope.getTwo() == null ? "*" : String.valueOf(timeScope.getTwo().getTime())) +
                        "]";
            }
        }

        Collection<String> selectedCategories = (Collection<String>) ctx.get("search_categories_selected");
        String categoriesFQ = "";
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            CollectionUtils.transform(selectedCategories, new Transformer() {
                public Object transform(Object input) {
                    return "asm_attr_s_subcategory:" + QueryParser.escape(String.valueOf(input));
                }
            });

            categoriesFQ = " +(" + StringUtils.join(selectedCategories, " ") + ")";
        }

        // фильтр на только новости
        params.add(CommonParams.FQ, "+type:news* +published:true" + categoriesFQ + timeScopeFQ);

        // пэйджинг
        int offset = ctx.get("search_start") != null ? (int) ctx.get("search_start") : 0;
        params.add(CommonParams.START, String.valueOf(offset));
        int limit = ctx.get("search_limit") != null ? (int) ctx.get("search_limit") : DEFAULT_MAX_RESULTS;
        params.add(CommonParams.ROWS, String.valueOf(limit));

        // нам нужны только поля:
        //  id - идентификатор
        //  score - "баллы" поиска, для сортировки по релевантности
        params.add(CommonParams.FL, "id,score");
        // выставляем сортировку по релевантности
        params.add(CommonParams.SORT, "score desc");

        QueryResponse queryResponse = solr.query(params);
        SolrDocumentList results = queryResponse.getResults();

        // TODO: переписать на селект типа: id IN (:ids)
        Collection<Asm> asms = new ArrayList<>(results.size());
        for (SolrDocument document : results) {
            asms.add(adao.asmSelectById(Long.valueOf(String.valueOf(document.getFieldValue("id")))));
        }

        ctx.put("search_result", asms);
    }
}
