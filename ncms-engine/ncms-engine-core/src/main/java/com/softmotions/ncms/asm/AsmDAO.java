package com.softmotions.ncms.asm;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.ncms.asm.events.AsmLockedEvent;
import com.softmotions.ncms.asm.events.AsmUnlockedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.weboot.mb.MBAction;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * Assembly access DAO.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("unchecked")
@Singleton
public class AsmDAO extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmDAO.class);

    private final SqlSessionFactory sessionFactory;

    private final NcmsEventBus ebus;


    @Inject
    public AsmDAO(SqlSession sess,
                  SqlSessionFactory sessionFactory,
                  NcmsEventBus ebus) {
        super(AsmDAO.class, sess);
        this.sessionFactory = sessionFactory;
        this.ebus = ebus;
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

    @Override
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
    public int coreDelete(@Nullable Long id, @Nullable String location) {
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

    /**
     * Locks the specified assembly.
     * If assembly is locked by another user
     * it does nothing and returns name of current lock holder
     * otherwise returns `null`.
     *
     * @param asmId Assembly id
     * @param user  Assembly lock holder
     * @return Current locker user name or `null`
     * if lock was updated successfully
     * or specified assembly is not found.
     */
    @Nullable
    @Transactional
    public String asmLock(Long asmId, String user, boolean silent) {
        ebus.unlockOnTxFinish(Asm.acquireLock(asmId));
        if (update("asmLock", "id", asmId, "user", user) < 1) {
            return selectOne("asmSelectLockUser", asmId);
        } else {
            AsmLockedEvent evt = new AsmLockedEvent(this, asmId, user);
            if (silent) {
                evt.hint("silent", true);
            }
            ebus.fireOnSuccessCommit(evt);
        }
        return null;
    }

    @Nullable
    public String asmLock(Long asmId, String user) {
        return asmLock(asmId, user, false);
    }

    /**
     * Unlock an assembly lock.
     * Returns `true` if existing lock
     * was cleared.
     *
     * @param asmId Assembly id
     * @return Returns `true` if existing lock
     * was cleared.
     */
    @Transactional
    public boolean asmUnlock(Long asmId, boolean silent) {
        ebus.unlockOnTxFinish(Asm.acquireLock(asmId));
        if (update("asmUnlock", asmId) > 0) {
            AsmUnlockedEvent evt = new AsmUnlockedEvent(this, asmId, null);
            if (silent) {
                evt.hint("silent", true);
            }
            ebus.fire(evt);
            return true;
        }
        return false;
    }

    /**
     * Unlock an assembly lock.
     * Returns `true` if an existing lock was owned by [user]
     * and successfully cleared.
     *
     * @param asmId Assembly id
     * @return Returns `true` if existing lock
     * was cleared.
     */
    @Transactional
    public boolean asmUnlock(Long asmId, String user, boolean silent) {
        ebus.unlockOnTxFinish(Asm.acquireLock(asmId));
        if (update("asmUnlock2", "id", asmId, "user", user) > 0) {
            AsmUnlockedEvent evt = new AsmUnlockedEvent(this, asmId, user);
            if (silent) {
                evt.hint("silent", true);
            }
            ebus.fire(evt);
            return true;
        }
        return false;
    }

    public boolean asmUnlock(Long asmId) {
        return asmUnlock(asmId, false);
    }

    public boolean asmUnlock(Long asmId, String user) {
        return asmUnlock(asmId, user, false);
    }

    /**
     * Select current assembly lock: `(username, lock date)`
     * or `null` if assembly is not locked.
     */
    @Nullable
    public Pair<String, Date> asmSelectLock(Long asmId) {
        Map<String, Object> ret = selectOne("asmSelectLock", asmId);
        if (ret == null) {
            return null;
        }
        return new ImmutablePair(ret.get("lock_user"), ret.get("lock_date"));
    }


    @Transactional
    public int asmUpdate(Asm asm) {
        return sess.update(toStatementId("asmUpdate"), asm);
    }

    /**
     * Clone assembly under new name and its attributes.
     * <p>
     * Note: No page/file dependencies are cloned.
     *
     * @param asmId       Source assembly id
     * @param name        Name of the new assembly clone
     * @param hname       Clone human name
     * @param description Clone description
     * @param skipTypes   Optional list of attribute types what will not be cloned.
     */
    @Transactional
    public Asm asmClone(long asmId,
                        String name,
                        String type,
                        String hname,
                        String description,
                        @Nullable String[] skipTypes) {

        Map<String, Object> params = new HashMap<>();
        params.put("asmId", asmId);
        params.put("name", name);
        params.put("hname", hname);
        params.put("description", description);
        params.put("type", type);
        insert("asmClone", params);
        Long newAsmId = (Long) params.get("id");
        if (newAsmId == null) {
            throw new RuntimeException("Cannot get generated key to ''asmClone' insert");
        }
        insert("asmCloneParents",
               "asmId", asmId,
               "newAsmId", newAsmId);
        insert("asmCloneAttrs",
               "asmId", asmId,
               "newAsmId", newAsmId,
               "skipTypes", skipTypes);
        insert("asmCloneSysProps",
               "asmId", asmId,
               "newAsmId", newAsmId);
        insert("asmCloneRefData",
               "asmId", asmId,
               "newAsmId", newAsmId);

        Asm asm = asmSelectById(newAsmId);
        if (asm == null) {
            throw new RuntimeException("No asm cloned");
        }
        return asm;
    }

    @Transactional
    public int asmSetParent(Asm asm, @Nullable Asm parent) {
        if (parent != null) {
            return asmSetParent(asm.id, parent.id);
        } else {
            return 0;
        }
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
    public int asmUpsertAttribute(AsmAttribute attr) {
        return update("upsertAttribute", attr);
    }

    @Transactional
    @Nullable
    public Asm asmSelectByName(String name) {
        return selectOne("selectAsmByCriteria",
                         "name", name);
    }


    @Transactional
    @Nullable
    public Long asmSelectIdByName(String name) {
        Number id = sess.selectOne(toStatementId("asmIDByName"), name);
        return id != null ? id.longValue() : null;
    }

    @Transactional
    @Nullable
    public Long asmSelectIdByAlias(String alias) {
        Number id = sess.selectOne(toStatementId("asmIDByAlias"), alias);
        return (id != null) ? id.longValue() : null;
    }

    @Transactional
    public void asmUpdateAlias(Long id, @Nullable String alias) {
        update("asmResetAlias", alias);
        update("asmUpdateAlias",
               "id", id,
               "alias", alias);
    }

    @Transactional
    public void asmUpdateAlias2(Long id, String alias) {
        update("asmResetAlias2", alias);
        update("asmUpdateAlias2",
               "id", id,
               "alias", alias);
    }

    @Transactional
    public boolean asmIsUniqueAlias(String alias, long asmId) {
        Number count = selectOne("asmIsUniqueAlias",
                                 "alias", alias,
                                 "id", asmId);
        return (count == null || count.intValue() < 1);
    }

    @Transactional
    @Nullable
    public String asmSelectNameById(Long id) {
        return sess.selectOne(toStatementId("asmNameByID"), id);
    }

    @Transactional
    @Nullable
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
        for (String r : roles) {
            if (!StringUtils.isBlank(r)) {
                rset.add(r);
            }
        }
        delete("deleteAsmAccessRoles", id);
        if (!rset.isEmpty()) {
            insert("insertAsmAccessRoles",
                   "id", id,
                   "roles", rset);
        }
    }

    @Nullable
    @Transactional
    public AsmAttribute asmAttributeByName(long asmId, String name) {
        return selectOne("asmAttributeByName",
                         "asmId", asmId,
                         "name", name);
    }

    @Nonnull
    @Transactional
    public List<AsmAttribute> asmAttributesByType(long asmId, String type) {
        return select("asmAttributesByType",
                      "asmId", asmId,
                      "type", type);
    }


    @Transactional
    public Collection<Asm> selectPageLayer(Long parent) {
        return select("selectPageLayer", "nav_parent_id", parent, "page_type", "page%");
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
        //full exclusive identity synchronization
        Lock lock = Asm.acquireLock(0L);
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
        } finally {
            lock.unlock();
        }
        return asm;
    }

    /**
     * Remove assembly instance with specified id
     *
     * @param asmId Assembly identifier
     * @return Number of updated rows
     */
    @Transactional
    public int asmRemove(Long asmId) {
        asmRemoveAllParents(asmId);
        return sess.delete("asmRemove", asmId);
    }

    /**
     * Remove specified assembly parent
     * from all chil assemblies
     *
     * @param asmId Assembly identifier
     * @return Number of updated rows
     */
    @Transactional
    public int asmRemoveParentFromAll(Long asmId) {
        return sess.delete("asmRemoveParentFromAll", asmId);
    }

    /**
     * Set a new assembly core
     *
     * @param asm      Assembly
     * @param location New assemly core location
     */
    @Nullable
    @Transactional
    public AsmCore asmSetCore(Asm asm, String location) {
        AsmCore core = null;
        if (StringUtils.isBlank(location)) {
            if (asm.getCore() != null) {
                coreDelete(asm.getCore().getId(), null);
                asm.setCore(null);
                update("asmUpdateCore",
                       "id", asm.getId(),
                       "coreId", null);
            }
            return null;
        }
        core = selectOne("selectAsmCore", "location", location);
        if (core == null) {
            core = new AsmCore(location);
            coreInsert(core);
        }
        update("asmUpdateCore",
               "id", asm.getId(),
               "coreId", core.getId());
        return core;
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
    @Nullable
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
        Number res = selectOne("selectChildrenCount", asmId);
        return res != null ? res.longValue() : 0L;
    }

    @Transactional
    public void asmSetEdate(long asmId, Date date) {
        update("asmSetEdate",
               "id", asmId,
               "edate", date);
    }

    @Nullable
    @Transactional
    public String asmSelectAliasByGuid(String guid) {
        Map<String, String> row = selectOne("asmSelectAliasByGuid", guid);
        if (row == null) {
            return null;
        }
        String alias = row.get("nav_alias");
        if (alias == null) {
            alias = row.get("nav_alias2");
        }
        return alias;
    }

    @Transactional
    @Nullable
    public Asm asmPlainByIdWithTemplates(long asmId, String... templates) {
        return selectOne("asmPlainByIdWithTemplates", "id", asmId, "templates", templates);
    }

    public PageCriteria newPageCriteria() {
        return new PageCriteria(this, this.namespace).withStatement("queryAttrs");
    }

    public void setAsmRefData(Long asmId, String type, Number ivalue) {
        setAsmRefData(asmId, type, null, ivalue);
    }

    public void setAsmRefData(Long asmId, String type, String svalue) {
        setAsmRefData(asmId, type, svalue, null);
    }

    @Transactional
    public void setAsmRefData(Long asmId, String type,
                              @Nullable String svalue, @Nullable Number ivalue) {
        if (svalue == null && ivalue == null) {
            throw new IllegalArgumentException("At least one of values must be not null");
        }

        update("setAsmRefData", "id", asmId, "type", type, "svalue", svalue, "ivalue", ivalue);
    }

    public static class Criteria extends CriteriaBase<Criteria> {
        public Criteria(AsmDAO dao, String namespace) {
            super(dao, namespace);
        }
    }
}
