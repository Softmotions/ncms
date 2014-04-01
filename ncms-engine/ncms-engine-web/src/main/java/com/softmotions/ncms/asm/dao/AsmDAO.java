package com.softmotions.ncms.asm.dao;

import com.softmotions.ncms.asm.Asm;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;

import java.util.List;

/**
 * Assembly access DAO.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class AsmDAO {

    private final SqlSession sess;

    public SqlSession getSession() {
        return sess;
    }

    @Inject
    public AsmDAO(SqlSession sess) {
        this.sess = sess;
    }

    @Transactional
    public List<Asm> selectAllAsm() {
        return sess.selectList("com.softmotions.ncms.AsmMapper.selectAllAsm");
    }

    @Transactional
    public int insertAsm(Asm asm) {
        return sess.insert("com.softmotions.ncms.AsmMapper.insertAsm", asm);
    }

    @Transactional
    public List<Asm> selectAsmByCriteria() {
        return sess.selectList("com.softmotions.ncms.AsmMapper.selectAsmByCriteria");
    }
}
