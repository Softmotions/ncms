package com.softmotions.ncms.db;

import com.softmotions.commons.weboot.mb.MBAction;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;
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

        AsmDAO.AsmCriteria cq = adao.newAsmCriteria()
                .orderBy("name").desc()
                .onAsmAttribute()
                .orderBy("type")
                .limit(100);

        asmList = cq.select();
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
        cq = adao.newAsmCriteria().pk(asm.getId());
        asm = cq.selectOne();
        assertNotNull(asm);
        assertEquals(asm.getId(), asm2.getId());
        assertEquals(asm.getName(), asm2.getName());

        //Find by NAME
        cq.clear();
        cq.param("name", "foo");
        asm = cq.selectOne();
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

        //Insert AsmCore
        AsmCore core = new AsmCore("file:///some/file", "my first assembly core");
        adao.insertAsmCore(core);

        //Select by criteria query
        AsmCore core2 = adao.newCriteria("location", "file:///some/file")
                .withStatement("selectAsmCore")
                .selectOne();

        assertNotNull(core2);
        assertEquals(core.getId(), core2.getId());
        assertEquals(core.getLocation(), core2.getLocation());

        core = new AsmCore("file:///some/file2", "the second assembly core");
        adao.insertAsmCore(core);

        //Update core
        core.setName(null);
        core.setLocation(null);
        core.setTemplateEngine("freemarker");
        assertEquals(1, adao.updateAsmCore(core));

        //Test attachment of core
        core = adao.newCriteria("name", "the second assembly core")
                .withStatement("selectAsmCore")
                .selectOne();
        assertNotNull(core);
        assertEquals("freemarker", core.getTemplateEngine());

        asm = adao.newAsmCriteria().param("name", "foo").selectOne();
        assertNotNull(asm);

        asm.setCore(core);
        assertEquals(1, adao.updateAsm(asm));

        asm = adao.newAsmCriteria().param("name", "foo").selectOne();
        assertNotNull(asm);
        assertNotNull(asm.getCore());
        assertEquals(core.getId(), asm.getCore().getId());
        assertEquals(core.getLocation(), asm.getCore().getLocation());
    }
}
