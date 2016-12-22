package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.CachedPage;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.mhttl.SelectNode;

/**
 * Select box controller
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
public class AsmSelectAM extends AsmAttributeManagerSupport {

    public static final String[] TYPES = new String[]{"select"};

    private final ObjectMapper mapper;

    private final PageService pageService;

    private final AsmDAO adao;

    @Inject
    public AsmSelectAM(ObjectMapper mapper, PageService pageService, AsmDAO adao) {
        this.mapper = mapper;
        this.pageService = pageService;
        this.adao = adao;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Nullable
    private ArrayNode checkFetchFrom(Asm page, AsmAttribute attr) {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        KVOptions opts = new KVOptions(attr.getOptions());
        String fetchFrom = opts.getString("fetchfrom");
        if (StringUtils.isBlank(fetchFrom)) {
            return null;
        }
        Long id = page.getId();
        CachedPage npPage = pageService.getCachedPage(id, true);
        if (npPage == null) {
            return null;
        }
        id = npPage.getNavParentId();
        if (id == null) {
            return null;
        }
        npPage = pageService.getCachedPage(id, true);
        if (npPage == null) {
            return null;
        }
        AsmAttribute fattr = npPage.getAsm().getEffectiveAttribute(fetchFrom);
        if (fattr == null || !"string".equals(fattr.getType())) {
            return null;
        }
        String value = fattr.getValue();
        if (StringUtils.isEmpty(value)) {
            return mapper.createArrayNode();
        }
        String[] items = value.split(",");
        ArrayNode res = mapper.createArrayNode();
        for (String item : items) {
            item = item.trim();
            ArrayNode el = mapper.createArrayNode();
            el.add(false);
            el.add(item);
            el.add(item);
            res.add(el);
        }
        return res;
    }

    @Override
    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {
        ArrayNode tarr = checkFetchFrom(page, attr);
        if (tarr == null && (tmplAttr == null || StringUtils.isBlank(tmplAttr.getEffectiveValue()))) {
            if (StringUtils.isBlank(attr.getEffectiveValue())) {
                attr.setEffectiveValue("[]");
            }
            return attr;
        }
        try {
            if (StringUtils.isBlank(attr.getEffectiveValue())) {
                attr.setEffectiveValue("[]");
            }
            Set<String> selectedKeys = new HashSet<>();
            ArrayNode vArr = (ArrayNode) mapper.readTree(attr.getEffectiveValue());
            for (JsonNode n : vArr) {
                if (!n.isArray()) {
                    continue;
                }
                ArrayNode aNode = (ArrayNode) n;
                boolean selected = aNode.get(0).asBoolean();
                if (selected) {
                    JsonNode key = aNode.get(1);
                    if (key != null) {
                        selectedKeys.add(key.asText());
                    }
                }
            }
            if (tarr == null) {
                tarr = (ArrayNode) mapper.readTree(tmplAttr.getEffectiveValue());
            }
            for (JsonNode n : tarr) {
                if (!n.isArray()) {
                    continue;
                }
                ArrayNode aNode = (ArrayNode) n;
                JsonNode key = aNode.get(1);
                if (key != null) {
                    aNode.set(0, mapper.getNodeFactory().booleanNode(selectedKeys.contains(key.asText())));
                }
            }
            attr.setEffectiveValue(mapper.writeValueAsString(tarr));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return attr;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        String value = attr.getEffectiveValue();

        if (StringUtils.isEmpty(value)) {
            return null;
        }

        Collection<String> items = new ArrayList<>();
        Object selectNodes = parseSelectNodes(value, false, false);
        if (selectNodes instanceof Iterable) {
            //noinspection ConstantConditions
            CollectionUtils.collect((Iterable<?>) selectNodes,
                                    input -> (input instanceof SelectNode) ?
                                             ((SelectNode) input).getValue() : null,
                                    items);
        } else if (selectNodes instanceof SelectNode) {
            items.add(((SelectNode) selectNodes).getValue());
        }

        return items.toArray(new String[items.size()]);
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null) {
            return Collections.EMPTY_LIST;
        }
        String value = attr.getEffectiveValue();
        if (StringUtils.isEmpty(value)) {
            return Collections.EMPTY_LIST;
        }
        boolean first = BooleanUtils.toBoolean(options.get("first"));
        boolean all = BooleanUtils.toBoolean(options.get("all"));
        return parseSelectNodes(value, first, all);
    }

    @Nullable
    private Object parseSelectNodes(String value, boolean first, boolean all) {
        List<SelectNode> nodes = first ? null : new ArrayList<>();
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                return Collections.EMPTY_LIST;
            }
            while (parser.nextToken() == JsonToken.START_ARRAY) {
                boolean selected = parser.nextBooleanValue();
                String key = parser.nextTextValue();
                String val = parser.nextTextValue();
                if (key != null) {
                    if (selected || all) {
                        if (first) {
                            return new SelectNode(key, val, selected);
                        } else {
                            nodes.add(new SelectNode(key, val, selected));
                        }
                    }
                }
                parser.nextToken(); //should be END_ARRAY
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return nodes;
    }


    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        //options
        JsonNode optsVal = val.get("display");
        AsmOptions opts = new AsmOptions();
        if (optsVal != null && optsVal.isTextual()) {
            opts.put("display", optsVal.asText());
        }
        optsVal = val.get("fetchfrom");
        if (optsVal != null && optsVal.isTextual()) {
            opts.put("fetchfrom", optsVal.asText());
        }
        optsVal = val.get("multiselect");
        if (optsVal != null && optsVal.isBoolean()) {
            opts.put("multiselect", optsVal.asText());
        }
        attr.setOptions(opts.toString());
        applyAttributeValue(ctx, attr, val);
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        JsonNode value = val.get("value");
        if (value != null && value.isArray()) {
            attr.setEffectiveValue(mapper.writeValueAsString(value));
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {
        if (val == null) {
            return;
        }
        JsonNode value = val.get("value");
        List<String> mvals = new ArrayList<>();
        if (value != null && value.isArray()) {
            ArrayNode arr = (ArrayNode) value;
            for (int i = 0, l = arr.size(); i < l; ++i) {
                if (!arr.get(i).isArray()) {
                    continue;
                }
                ArrayNode slot = (ArrayNode) arr.get(i);
                if (slot.size() != 3 || !slot.get(0).isBoolean() || !slot.get(0).asBoolean()) {
                    continue;
                }
                mvals.add(slot.get(2).asText());
            }
        }
        adao.updateAttrsIdxStringValues(attr, mvals);
    }
}
