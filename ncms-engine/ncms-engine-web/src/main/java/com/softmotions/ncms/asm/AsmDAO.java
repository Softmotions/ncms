package com.softmotions.ncms.asm;

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
        return new Criteria(this, namespace).params(params);
    }

    public AsmCriteria newAsmCriteria() {
        return new AsmCriteria(this, namespace).withStatement("selectAsmByCriteria");
    }

    public AsmCriteria newAsmCriteria(Object... params) {
        return new AsmCriteria(this, namespace).params(params).withStatement("selectAsmByCriteria");
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
    public int insertAsmCore(AsmCore core) {
        return sess.insert(toStatementId("insertAsmCore"), core);
    }

    @Transactional
    public int updateAsmCore(AsmCore core) {
        return sess.update(toStatementId("updateAsmCore"), core);
    }

    @Transactional
    public int delAsmCore(Long id, String location) {
        AsmCore core = new AsmCore();
        if (id != null) {
            core.id = id;
        } else if (location != null) {
            core.location = location;
        }
        return sess.delete(toStatementId("delAsmCore"), core);
    }

    @Transactional
    public int insertAsm(Asm asm) {
        return sess.insert(toStatementId("insertAsm"), asm);
    }

    @Transactional
    public int updateAsm(Asm asm) {
        return sess.update(toStatementId("updateAsm"), asm);
    }

    @Transactional
    public int insertAsmAttribute(Asm asm, AsmAttribute attr) {
        attr.asmId = asm.id;
        return sess.insert(toStatementId("insertAsmAttribute"), attr);
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
