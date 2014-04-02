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
import java.util.Map;

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

    public AsmCriteria newAsmCriteria() {
        return new AsmCriteria(namespace);
    }

    public AsmCriteria newAsmCriteria(Object... params) {
        return new AsmCriteria(namespace).params(params);
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
    public int insertAsmAttribute(Asm asm, AsmAttribute attr) {
        attr.asmId = asm.id;
        return sess.insert(toStatementId("insertAsmAttribute"), attr);
    }

    @Transactional
    public List<Asm> selectAsmByCriteria(AsmCriteria cq) {
        return selectByCriteria(cq, "selectAsmByCriteria");
    }

    @Transactional
    public Asm selectOneAsmByCriteria(AsmCriteria cq) {
        return selectOneByCriteria(cq, "selectAsmByCriteria");
    }

    public static class AsmCriteria extends MBCriteriaQuery<AsmCriteria> {

        public AsmCriteria(String namespace) {
            super(namespace);
        }

        public AsmCriteria(String namespace, Map<String, Object> params) {
            super(namespace, params);
        }

        public AsmCriteria onAsm() {
            prefixedBy(null);
            return this;
        }

        public AsmCriteria onAsmAttribute() {
            prefixedBy("ATTR_");
            return this;
        }
    }

}
