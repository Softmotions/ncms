package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.mhttl.SelectNode;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.solr.client.solrj.SolrServer;

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
@SuppressWarnings("unchecked")
@Singleton
public class SearchNewsController extends SearchController {

    protected static final Map<String, Callable<Pair<Date, Date>>> TIME_SCOPES;
    protected static final String DEFAULT_TIME_SCOPE = "all";

    static {
        TIME_SCOPES = new HashMap<>();
        TIME_SCOPES.put(SearchNewsController.DEFAULT_TIME_SCOPE, new Callable<Pair<Date, Date>>() {
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
        super(adao, solr);
    }

    protected void prepare(AsmRendererContext ctx) {
        super.prepare(ctx);

        HttpServletRequest req = ctx.getServletRequest();

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

    protected String getCustomFields() {
        return "asm_attr_image_icon,asm_attr_l_event_date,asm_attr_s_subcategory";
    }

    protected String buildFilterQuery(AsmRendererContext ctx) throws Exception {
        // фильтр на категорию новости
        String categoriesFQ = "";
        Collection<String> selectedCategories = (Collection<String>) ctx.get("search_categories_selected");
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            CollectionUtils.transform(selectedCategories,
                                      input -> "asm_attr_s_subcategory:" + QueryParser.escape(String.valueOf(input)));

            categoriesFQ = " +(" + StringUtils.join(selectedCategories, " ") + ")";
        }

        // фильтр на дату создания
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

        // добавляем фильтр на только новости, на категорию носвостей (если есть) и на дату создания
        return "+type:news* " + super.buildFilterQuery(ctx) + categoriesFQ + timeScopeFQ;
    }
}
