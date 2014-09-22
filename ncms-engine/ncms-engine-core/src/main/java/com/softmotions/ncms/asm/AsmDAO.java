package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.Pair;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.weboot.mb.MBAction;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assembly access DAO.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Singleton
public class AsmDAO extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmDAO.class);

    private final SqlSessionFactory sessionFactory;

    @Inject
    public AsmDAO(SqlSession sess, SqlSessionFactory sessionFactory) {
        super("com.softmotions.ncms.asm.AsmDAO", sess);
        this.sessionFactory = sessionFactory;
    }

    public Criteria newCriteria() {
        return new Criteria(this, namespace);
    }

    public Criteria newCriteria(Object... params) {
        return new Criteria(this, namespace)
                .withParams(params);
    }

    public AsmCriteria newAsmCriteria() {
        return new AsmCriteria(this, namespace)
                .withStatement("selectAsmByCriteria");
    }

    public AsmCriteria newAsmCriteria(Object... params) {
        return new AsmCriteria(this, namespace)
                .withParams(params)
                .withStatement("selectAsmByCriteria");
    }

    @Transactional
    public <T> T withinTransaction(MBAction<T> action) throws SQLException {
        return super.withinTransaction(action);
    }

    @Transactional
    public List<Asm> asmSelectAllPlain() {
        return sess.selectList(toStatementId("asmSelectAllPlain"));
    }

    @Transactional
    public int coreInsert(AsmCore core) {
        return sess.insert(toStatementId("coreInsert"), core);
    }

    @Transactional
    public int coreUpdate(AsmCore core) {
        return sess.update(toStatementId("coreUpdate"), core);
    }

    @Transactional
    public int coreDelete(Long id, String location) {
        AsmCore core = new AsmCore();
        if (id != null) {
            core.id = id;
        } else if (location != null) {
            core.location = location;
        }
        return sess.delete(toStatementId("coreDelete"), core);
    }

    @Transactional
    public int asmInsert(Asm asm) {
        if (asm.getCore() != null) {
            if (asm.getCore().getId() == null) {
                coreInsert(asm.getCore());
            } else {
                coreUpdate(asm.getCore());
            }
        }
        int ret = sess.insert(toStatementId("asmInsert"), asm);
        if (asm.getAttributes() != null) {
            for (AsmAttribute attr : asm.getAttributes()) {
                asmSetAttribute(asm, attr);
            }
        }
        return ret;
    }

    @Transactional
    public int asmUpdate(Asm asm) {
        return sess.update(toStatementId("asmUpdate"), asm);
    }

    @Transactional
    public int asmSetParent(Asm asm, Asm parent) {
        return asmSetParent(asm.id, parent.id);
    }

    @Transactional
    public int asmSetParent(long asmId, long parentId) {
        TinyParamMap params = new TinyParamMap()
                .param("asmId", asmId)
                .param("parentId", parentId);
        Number cnt = sess.selectOne(toStatementId("asmHasSpecificParent"), params);
        if (cnt.intValue() > 0) {
            return 0; //we have this parent
        }
        return sess.insert("asmSetParent", params);
    }

    @Transactional
    public int asmRemoveParent(long asmId, long parent) {
        TinyParamMap params = new TinyParamMap()
                .param("asmId", asmId)
                .param("parentId", parent);
        return sess.delete(toStatementId("asmRemoveParent"), params);
    }

    @Transactional
    public int asmRemoveAllParents(long asmId) {
        return delete("asmRemoveAllParents", "asmId", asmId);
    }

    @Transactional
    public int asmSetAttribute(Asm asm, AsmAttribute attr) {
        attr.asmId = asm.id;
        return sess.insert(toStatementId("asmSetAttribute"), attr);
    }

    @Transactional
    public Asm asmSelectByName(String name) {
        return selectOne("selectAsmByCriteria",
                         "name", name);
    }


    @Transactional
    public Long asmSelectIdByName(String name) {
        Number id = sess.selectOne(toStatementId("asmIDByName"), name);
        return id != null ? id.longValue() : null;
    }

    @Transactional
    public Long asmSelectIdByAlias(String alias) {
        Number id = sess.selectOne(toStatementId("asmIDByAlias"), alias);
        return id != null ? id.longValue() : null;
    }

    @Transactional
    public boolean asmCheckUniqueAlias(long aliasId, String alias) {
        Number count = selectOne("asmCheckUniqueAlias", "aliasId", aliasId, "alias", alias);
        return 0 == count.intValue();
    }

    @Transactional
    public String asmSelectNameById(Long id) {
        return sess.selectOne(toStatementId("asmNameByID"), id);
    }

    @Transactional
    public Asm asmSelectById(Number id) {
        return selectOne("selectAsmByCriteria",
                         "id", id);
    }

    @Transactional
    public int asmRename(long id, String name) {
        return update("asmRename",
                      "id", id,
                      "name", name);
    }

    @Transactional
    public Collection<String> asmAccessRoles(long id) {
        List<String> res = select("asmAccessRoles", id);
        Collections.sort(res);
        return res.isEmpty() ? Collections.EMPTY_LIST : res;
    }

    @Transactional
    public void setAsmAccessRoles(long id, String... roles) {
        Set<String> rset = new HashSet<>(roles.length);
        Collections.addAll(rset, roles);
        delete("deleteAsmAccessRoles", id);
        if (roles.length > 0) {
            insert("insertAsmAccessRoles",
                   "id", id,
                   "roles", rset);
        }
    }

    /**
     * Insert new assembly with generated name
     *
     * @return
     */
    public Asm asmInsertEmptyNew(String namePrefix) {
        if (namePrefix == null) {
            namePrefix = "New assembly";
        }
        Asm asm = new Asm();
        String name;
        synchronized (Asm.class) { //full exclusive identity synchronization
            try (SqlSession s = sessionFactory.openSession()) {
                Number existId;
                int i = 1;
                do {
                    name = namePrefix + i++;
                    existId = s.selectOne(toStatementId("asmIDByName"), name);
                    if (existId == null) {
                        asm.setName(name);
                        asmInsert(asm);
                    }
                } while (existId != null);
                s.commit(true);
            }
        }
        return asm;
    }

    /**
     * Remove assembly instance with specified id
     *
     * @param asmId Assembly identifier
     * @return Number of updated rows
     */
    public int asmRemove(Long asmId) {
        return sess.delete("asmRemove", asmId);
    }

    /**
     * Remove specified assembly parent
     * from all chil assemblies
     *
     * @param asmId Assembly identifier
     * @return Number of updated rows
     */
    public int asmRemoveParentFromAll(Long asmId) {
        return sess.delete("asmRemoveParentFromAll", asmId);
    }


    @Transactional
    public void asmSetSysprop(Long asmId, String property, String value) {
        delete("asmDropSysprop",
               "asmId", asmId,
               "property", property);
        insert("asmInsertSysprop",
               "asmId", asmId,
               "property", property,
               "value", value);
    }

    @Transactional
    public void asmDropSysprop(Long asmId, String property) {
        delete("asmDropSysprop",
               "asmId", asmId,
               "property", property);
    }

    @Transactional
    public void asmDropAllSysprops(Long asmId) {
        delete("asmDropAllSysprops",
               "asmId", asmId);
    }

    @Transactional
    public String asmSelectSysprop(Long asmId, String property) {
        return selectOne("asmSelectSysprop",
                         "asmId", asmId,
                         "property", property);
    }


    @Transactional
    public void updateAttrsIdxStringValues(AsmAttribute attr, Collection<String> values) {
        if (attr.getId() == null) {
            throw new IllegalArgumentException("Attribute instance with unspecified 'id' property");
        } else {
            update("deleteAttrsIdxValues", attr.getId());
            if (!values.isEmpty()) {
                insert("insertAttrsIdxValues",
                       "attrId", attr.getId(),
                       "values", values);
            }
        }
    }

    @Transactional
    public void updateAttrsIdxNumberValues(AsmAttribute attr, Collection<Long> values) {
        if (attr.getId() == null) {
            throw new IllegalArgumentException("Attribute instance with unspecified 'id' property");
        } else {
            update("deleteAttrsIdxValues", attr.getId());
            if (!values.isEmpty()) {
                insert("insertAttrsIdxIValues",
                       "attrId", attr.getId(),
                       "values", values);
            }
        }
    }


    @Transactional
    public void bumpAsmOrdinal(long asmId) {
        update("bumpAsmOrdinal", asmId);
    }


    @Transactional
    public long asmChildrenCount(long asmId) {
        return ((Number) selectOne("selectChildrenCount", asmId)).longValue();
    }

    public PageCriteria newPageCriteria() {
        return new PageCriteria(this, this.namespace).withStatement("queryAttrs");
    }

    @SuppressWarnings("unchecked")
    static class CriteriaBase<T extends CriteriaBase> extends MBCriteriaQuery<T> {

        CriteriaBase(MBDAOSupport dao, String namespace) {
            super(dao, namespace);
        }

        public T onAsm() {
            prefixedBy("ASM_");
            return (T) this;
        }

        public T onAsmAttribute() {
            prefixedBy("ATTR_");
            return (T) this;
        }

        public T onAsmCore() {
            prefixedBy("CORE_");
            return (T) this;
        }
    }

    public static class Criteria extends CriteriaBase<Criteria> {
        public Criteria(AsmDAO dao, String namespace) {
            super(dao, namespace);
        }
    }

    public static class AsmCriteria extends CriteriaBase<AsmCriteria> {

        public AsmCriteria(AsmDAO dao, String namespace) {
            super(dao, namespace);
        }

        public AsmCriteria onAsm() {
            prefixedBy(null);
            return this;
        }
    }

    public static class PageCriteria extends CriteriaBase<PageCriteria> {

        private List<Pair<String, Object>> attrs = new ArrayList<>();

        private List<String> attrsInclude;

        private List<String> attrsExclude;

        public PageCriteria(AsmDAO dao, String namespace) {
            super(dao, namespace);
            withStatement("queryByAttrs");
        }

        public PageCriteria withPublished(boolean val) {
            return withParam("published", val);
        }

        public PageCriteria withTypeLike(String type) {
            return withParam("type", type);
        }

        public PageCriteria withAttributeLike(String name, Object val) {
            attrs.add(new Pair<>(name, val));
            return this;
        }

        public PageCriteria withTemplates(String... templates) {
            return withParam("templates", templates);
        }

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

        public PageCriteria onAsm() {
            return prefixedBy("asm.");
        }

        public PageCriteria attributesInclude(String... names) {
            if (attrsInclude == null) {
                attrsInclude = new ArrayList<>();
                withParam("attrsInclude", attrsInclude);
            }
            Collections.addAll(attrsInclude, names);
            return this;
        }

        public PageCriteria attrubutesExclude(String... names) {
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


        public Collection<Asm> selectAsAsms() {
            final Map<Long, Asm> asmGroup = new LinkedHashMap<>();
            //noinspection InnerClassTooDeeplyNested
            select(new ResultHandler() {
                public void handleResult(ResultContext context) {
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
                        asmGroup.put(id, asm);
                    }
                    String attrName = (String) row.get("attr_name");
                    if (attrName != null && asm.getAttribute(attrName) == null) {
                        AsmAttribute attr = new AsmAttribute();
                        attr.setId(((Number) row.get("attr_id")).longValue());
                        attr.setName(attrName);
                        attr.setType((String) row.get("attr_type"));
                        attr.setValue((String) row.get("attr_value"));
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
                }
            });
            return asmGroup.values();
        }
    }

}
