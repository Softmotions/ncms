package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.weboot.mb.MBAction;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.guice.transactional.Transactional;

import java.sql.SQLException;
import java.util.List;

/**
 * Assembly access DAO.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmDAO extends MBDAOSupport {

    final SqlSessionFactory sessionFactory;

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
        return sess.delete("asmRemoveParent", params);
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

}
