package com.softmotions.ncms.asm;

import com.softmotions.commons.weboot.mb.MBAction;
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
        super(sess);
    }

    @Transactional
    public <T> T withinTransaction(MBAction<T> action) throws SQLException {
        return super.withinTransaction(action);
    }

    @Transactional
    public List<Asm> selectAllPlainAsms() {
        return sess.selectList("com.softmotions.ncms.AsmMapper.selectAllPlainAsms");
    }

    @Transactional
    public int insertAsm(Asm asm) {
        return sess.insert("com.softmotions.ncms.AsmMapper.insertAsm", asm);
    }

    @Transactional
    public int insertAsmAttribute(Asm asm, AsmAttribute attr) {
        attr.asmId = asm.id;
        return sess.insert("com.softmotions.ncms.AsmMapper.insertAsmAttribute", attr);
    }

    @Transactional
    public List<Asm> selectAsmByCriteria(ASMCriteriaQuery cq) {
        cq.finish();
        if (cq.getStatement() != null) {
            return sess.selectList(cq.getStatement(), cq);
        } else {
            return sess.selectList("com.softmotions.ncms.AsmMapper.selectAsmByCriteria", cq);
        }
    }

    @Transactional
    public Asm selectAsmByCriteriaOne(ASMCriteriaQuery cq) {
        cq.finish();
        if (cq.getStatement() != null) {
            return sess.selectOne(cq.getStatement(), cq);
        } else {
            return sess.selectOne("com.softmotions.ncms.AsmMapper.selectAsmByCriteria", cq);
        }
    }
}
