package com.softmotions.ncms.asm;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.commons.cont.Pair;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class PageCriteria extends CriteriaBase<PageCriteria> {

    private static final Logger log = LoggerFactory.getLogger(PageCriteria.class);

    private List<Pair<String, Object>> attrs = new ArrayList<>();

    private List<String> attrsInclude;

    private List<String> attrsExclude;

    private List<String> notNullAttributes;

    public PageCriteria(AsmDAO dao, String namespace) {
        super(dao, namespace);
        withStatement("queryAttrs");
    }

    public PageCriteria withPublished(boolean val) {
        return withParam("published", val);
    }

    public PageCriteria withEdateLTE(Date date) {
        return withParam("edateLTE", date);
    }

    public PageCriteria withEdateGTE(Date date) {
        return withParam("edateGTE", date);
    }

    public PageCriteria withTypeLike(String type) {
        return withParam("type", type);
    }

    public PageCriteria withLang(String lang) {
        return withParam("lang", lang);
    }

    public PageCriteria withAlias(String alias) {
        return withParam("alias", alias);
    }

    public PageCriteria withOwner(String owner) {
        return withParam("owner", owner);
    }

    public PageCriteria withAttributeLike(String name, Object val) {
        attrs.add(new Pair<>(name, val));
        return this;
    }

    public PageCriteria withNotNullAttributes(String... names) {
        if (notNullAttributes == null) {
            notNullAttributes = new ArrayList<>();
            withParam("nnAttrs", notNullAttributes);
        }
        Collections.addAll(notNullAttributes, names);
        return this;
    }

    public PageCriteria withTemplates(String... templates) {
        return withParam("templates", templates);
    }

    @Override
    public PageCriteria finish() {
        withParam("attrs", attrs);
        if (!containsKey("attrsExclude")) {
            withParam("attrsExclude", Collections.EMPTY_LIST);
        }
        if (!containsKey("attrsInclude")) {
            withParam("attrsInclude", Collections.EMPTY_LIST);
        }
        return super.finish();
    }

    @Override
    public PageCriteria onAsm() {
        return prefixedBy("asm.");
    }

    public PageCriteria withAttributes(String... names) {
        if (attrsInclude == null) {
            attrsInclude = new ArrayList<>();
            withParam("attrsInclude", attrsInclude);
        }
        Collections.addAll(attrsInclude, names);
        return this;
    }

    public PageCriteria withoutAttributes(String... names) {
        if (attrsExclude == null) {
            attrsExclude = new ArrayList<>();
            withParam("attrsExclude", attrsExclude);
        }
        Collections.addAll(attrsExclude, names);
        return this;
    }

    public PageCriteria withNavParentId(Long id) {
        return withParam("navParentId", id);
    }


    public PageCriteria withLargeAttrValues() {
        return withParam("largeAttrValues", true);
    }

    @Nullable
    public Asm selectOneAsm() {
        Collection<Asm> asms = selectAsAsms();
        if (asms.isEmpty()) {
            return null;
        }
        return asms.iterator().next();
    }

    public Collection<Asm> selectAsAsms() {
        final Map<Long, Asm> asmGroup = new LinkedHashMap<>();
        //noinspection InnerClassTooDeeplyNested
        select(context -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = (Map<String, Object>) context.getResultObject();
            long id = ((Number) row.get("id")).longValue();
            Asm asm = asmGroup.get(id);
            if (asm == null) {
                asm = new Asm(id, (String) row.get("name"));
                asm.setHname((String) row.get("hname"));
                asm.setNavParentId((Long) row.get("nav_parent_id"));
                asm.setType((String) row.get("type"));
                asm.setMdate((Date) row.get("mdate"));
                asm.setCdate((Date) row.get("cdate"));
                asm.setEdate((Date) row.get("edate"));
                asmGroup.put(id, asm);
            }
            String attrName = (String) row.get("attr_name");
            if (attrName != null && asm.getAttribute(attrName) == null) {
                AsmAttribute attr = new AsmAttribute();
                attr.setId(((Number) row.get("attr_id")).longValue());
                attr.setName(attrName);
                attr.setType((String) row.get("attr_type"));
                attr.setValue((String) row.get("attr_value"));

                // Process large attribute value
                Object lv = row.get("attr_large_value");
                if (lv instanceof String) {
                    attr.setLargeValue((String) lv);
                } else if (lv instanceof Clob) {
                    Clob clv = (Clob) row.get("attr_large_value");
                    if (clv != null) {
                        Reader lvr = null;
                        try {
                            lvr = clv.getCharacterStream();
                            attr.setLargeValue(IOUtils.toString(lvr));
                        } catch (IOException | SQLException e) {
                            throw new RuntimeException(e);
                        } finally {
                            if (lvr != null) {
                                try {
                                    lvr.close();
                                } catch (IOException e) {
                                    log.error("", e);
                                }
                            }
                        }
                    }
                }
                asm.addAttribute(attr);
            }
            if (row.get("np_name") != null && asm.getAttribute("np_name") == null) {
                asm.addAttribute(new AsmAttribute("np_name", "string", row.get("np_name").toString()));
            }
            if (row.get("np_hname") != null && asm.getAttribute("np_hname") == null) {
                asm.addAttribute(new AsmAttribute("np_hname", "string", row.get("np_hname").toString()));
            }
            if (row.get("np_id") != null && asm.getAttribute("np_id") == null) {
                asm.addAttribute(new AsmAttribute("np_id", "string", row.get("np_id").toString()));
            }
            if (row.get("p_name") != null && asm.getAttribute("p_name") == null) {
                asm.addAttribute(new AsmAttribute("p_name", "string", row.get("p_name").toString()));
            }
        });
        return asmGroup.values();
    }
}
