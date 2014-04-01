package com.softmotions.ncms.db;

import com.softmotions.commons.weboot.mb.MBAction;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.asm.ASMCriteriaQuery;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;

import com.google.inject.Injector;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;

public class NcmsModelAsmTest extends NcmsWebTest {

    private static final Logger log = LoggerFactory.getLogger(NcmsModelAsmTest.class);

    @Test
    public void testBasicAsmOperations() throws Exception {
        Injector injector = getInjector();
        AsmDAO adao = injector.getInstance(AsmDAO.class);

        Asm asm = new Asm();
        asm.setName("foo");
        assertEquals(1, adao.insertAsm(asm));

        List<Asm> asmList = adao.selectAllPlainAsms();
        assertFalse(asmList.isEmpty());

        Asm asm2 = asmList.get(0);
        assertNotSame(asm, asm2);
        assertEquals(asm.getId(), asm2.getId());
        assertEquals(asm.getName(), asm2.getName());
        assertEquals(asm.getDescription(), asm2.getDescription());

        boolean hasException = false;
        try {
            adao.insertAsm(asm);
        } catch (Exception e) {
            hasException = true;
            assertTrue(e instanceof PersistenceException);
        }
        assertTrue(hasException);

        AsmAttribute attr1 = new AsmAttribute("name1", "type1", "val1");
        assertEquals(1, adao.insertAsmAttribute(asm, attr1));

        AsmAttribute attr2 = new AsmAttribute("name2", "type2", "val2");
        assertEquals(1, adao.insertAsmAttribute(asm, attr2));

        ASMCriteriaQuery cq =
                new ASMCriteriaQuery()
                        .orderBy("name").desc()
                        .onAsmAttribute()
                        .orderBy("type")
                        .limit(100);

        asmList = adao.selectAsmByCriteria(cq);
        assertEquals(1, asmList.size());

        asm = asmList.get(0);
        assertEquals(asm.getId(), asm2.getId());
        assertEquals(asm.getName(), asm2.getName());
        assertEquals(asm.getDescription(), asm2.getDescription());
        assertNotNull(asm.getAttributes());

        assertEquals(2, asm.getAttributes().size());

        AsmAttribute attr = asm.getAttribute("name1");
        assertNotNull(attr1);
        assertEquals(attr1.getType(), attr.getType());
        assertEquals(attr1.getValue(), attr.getValue());

        attr = asm.getAttribute("name2");
        assertNotNull(attr2);
        assertEquals(attr2.getType(), attr.getType());
        assertEquals(attr2.getValue(), attr.getValue());

        hasException = false;
        try {
            adao.insertAsmAttribute(asm, attr2);
        } catch (Exception e) {
            hasException = true;
            assertTrue(e instanceof PersistenceException);
        }
        assertTrue(hasException);

        //Find by PK
        cq = new ASMCriteriaQuery().pk(asm.getId());
        asm = adao.selectAsmByCriteriaOne(cq);
        assertNotNull(asm);
        assertEquals(asm.getId(), asm2.getId());
        assertEquals(asm.getName(), asm2.getName());

        //Find by NAME
        cq.clear();
        cq.param("name", "foo");
        asm = adao.selectAsmByCriteriaOne(cq);
        assertNotNull(asm);
        assertEquals(asm.getId(), asm2.getId());
        assertEquals(asm.getName(), asm2.getName());

        //Test Raw JDBC access within MyBatis transaction
        Number count = adao.withinTransaction(new MBAction<Number>() {
            public Number exec(SqlSession sess, Connection conn) throws SQLException {
                Number ret = 0;
                try (Statement stmt = conn.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ASM_ATTRS")) {
                        if (rs.next()) {
                            ret = (Number) rs.getObject(1);
                        }
                    }
                }
                return ret;
            }
        });
        assertEquals(2, count.intValue());
    }
}
