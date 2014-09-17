package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.mhttl.SelectNode;

import com.google.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.solr.client.solrj.SolrServer;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public class SearchNewsController extends SearchController {

    @Inject
    public SearchNewsController(AsmDAO adao, SolrServer solr) {
        super(adao, solr);
    }

    protected void prepareInternal(AsmRendererContext ctx) {
        super.prepareInternal(ctx);

        HttpServletRequest req = ctx.getServletRequest();

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

    protected String buildFilterQuery(AsmRendererContext ctx) throws Exception {
        // фильтр на категорию новости
        String categoriesFQ = "";
        Collection<String> selectedCategories = (Collection<String>) ctx.get("search_categories_selected");
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            CollectionUtils.transform(selectedCategories, new Transformer() {
                public Object transform(Object input) {
                    return "asm_attr_s_subcategory:" + QueryParser.escape(String.valueOf(input));
                }
            });

            categoriesFQ = " +(" + StringUtils.join(selectedCategories, " ") + ")";
        }

        // добавляем фильтр на только новости  и на категорию носвостей (если есть)
        return "+type:news* " + super.buildFilterQuery(ctx) + categoriesFQ;
    }
}
