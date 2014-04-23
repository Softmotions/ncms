package com.softmotions.ncms.asm;

import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.weboot.mb.MBAction;
import com.softmotions.commons.weboot.mb.MBCriteriaQuery;
import com.softmotions.commons.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.ibatis.session.SqlSession;
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

    @Inject
    public AsmDAO(SqlSession sess) {
        super("com.softmotions.ncms.AsmMapper", sess);
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
    public List<Asm> selectAllPlainAsms() {
        return sess.selectList(toStatementId("selectAllPlainAsms"));
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
        TinyParamMap params = new TinyParamMap()
                .param("asmId", asm.id)
                .param("parentId", parent.id);
        Number cnt = sess.selectOne(toStatementId("asmHasSpecificParent"), params);
        if (cnt.intValue() > 0) {
            return 0; //we have this parent
        }
        return sess.insert("asmSetParent", params);
    }

    @Transactional
    public int asmSetAttribute(Asm asm, AsmAttribute attr) {
        attr.asmId = asm.id;
        return sess.insert(toStatementId("asmSetAttribute"), attr);
    }

    @Transactional
    public Asm selectAsmByName(String name) {
        return selectOne("selectAsmByCriteria",
                         "name", name);
    }

    @Transactional
    public Asm selectAsmById(Number id) {
        return selectOne("selectAsmByCriteria",
                         "id", id);
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
