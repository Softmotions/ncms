package com.softmotions.ncms.db;

import com.softmotions.weboot.mb.MBAction;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmCore;
import com.softmotions.ncms.asm.AsmDAO;

import com.google.inject.Inject;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class NcmsModelAsmTest extends NcmsWebTest {

    @Inject
    AsmDAO adao;

    @Test
    public void testBasicAsmOperations() throws Exception {
        Asm asm = new Asm();
        asm.setName("foo");
        assertEquals(1, adao.asmInsert(asm));

        List<Asm> asmList = adao.asmSelectAllPlain();
        assertFalse(asmList.isEmpty());

        Asm asm2 = asmList.get(0);
        assertNotSame(asm, asm2);
        assertEquals(asm.getId(), asm2.getId());
        assertEquals(asm.getName(), asm2.getName());
        assertEquals(asm.getDescription(), asm2.getDescription());

        boolean hasException = false;
        try {
            adao.asmInsert(asm);
        } catch (Exception e) {
            hasException = true;
            assertTrue(e instanceof PersistenceException);
        }
        assertTrue(hasException);

        AsmAttribute attr1 = new AsmAttribute("name1", "type1", "val1");
        assertEquals(1, adao.asmSetAttribute(asm, attr1));

        AsmAttribute attr2 = new AsmAttribute("name2", "type2", "val2");
        assertEquals(1, adao.asmSetAttribute(asm, attr2));

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
            adao.asmSetAttribute(asm, attr2);
        } catch (Exception e) {
            hasException = true;
            assertTrue(e instanceof PersistenceException);
        }
        assertTrue(hasException);

        //Find by PK
        cq = adao.newAsmCriteria().withPK(asm.getId());
        asm = cq.selectOne();
        assertNotNull(asm);
        assertEquals(asm.getId(), asm2.getId());
        assertEquals(asm.getName(), asm2.getName());

        //Find by NAME
        cq.clear();
        cq.withParam("name", "foo");
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
        adao.coreInsert(core);

        //Select by criteria query
        AsmCore core2 = adao.newCriteria("location", "file:///some/file")
                .withStatement("selectAsmCore")
                .selectOne();

        assertNotNull(core2);
        assertEquals(core.getId(), core2.getId());
        assertEquals(core.getLocation(), core2.getLocation());

        core = new AsmCore("file:///some/file2", "the second assembly core");
        adao.coreInsert(core);

        //Update core
        core.setName(null);
        core.setLocation(null);
        core.setTemplateEngine("freemarker");
        assertEquals(1, adao.coreUpdate(core));

        //Test attachment of core
        core = adao.newCriteria("name", "the second assembly core")
                .withStatement("selectAsmCore")
                .selectOne();
        assertNotNull(core);
        assertEquals("freemarker", core.getTemplateEngine());

        asm = adao.newAsmCriteria()
                .withParam("name", "foo")
                .selectOne();
        assertNotNull(asm);

        asm.setCore(core);
        assertEquals(1, adao.asmUpdate(asm));

        asm = adao.newAsmCriteria()
                .withParam("name", "foo")
                .selectOne();
        assertNotNull(asm);
        assertNotNull(asm.getCore());
        assertEquals(core.getId(), asm.getCore().getId());
        assertEquals(core.getLocation(), asm.getCore().getLocation());

         /*
         Test assemblies inheritance:

            p[0]
             /\
            /  \
         p[1]  p[2]
           \    /
             \/
             asm
         */
        Asm[] parentsArr = new Asm[]{
                new Asm("p[0]"),
                new Asm("p[1]"),
                new Asm("p[2]")
        };
        for (Asm a : parentsArr) {
            adao.asmInsert(a);
        }
        parentsArr[0].setCore(core);
        adao.asmUpdate(parentsArr[0]);
        adao.asmSetParent(parentsArr[1], parentsArr[0]);
        adao.asmSetParent(parentsArr[2], parentsArr[0]);
        adao.asmSetParent(asm, parentsArr[1]);
        adao.asmSetParent(asm, parentsArr[2]);
        adao.asmSetAttribute(parentsArr[2], new AsmAttribute("p[2]attr", "p[2]type", "p[2]value"));

        asm = adao.newAsmCriteria()
                .withPK(asm.getId())
                .selectOne();
        assertNotNull(asm);

        //Toggle lazy loading of parents
        List<Asm> parents = asm.getParents();
        assertEquals(2, parents.size());

        for (int i = 0, l = parents.size(); i < l; ++i) {
            Asm p = parents.get(i);
            assertTrue("p[1]".equals(p.getName()) || "p[2]".equals(p.getName()));
            if ("p[2]".equals(p.getName())) {
                assertNotNull(p.getAttributes());
                assertEquals(1, p.getAttributes().size());
                AsmAttribute a = p.getAttributes().iterator().next();
                assertEquals("p[2]attr", a.getName());
                assertEquals("p[2]type", a.getType());
                assertEquals("p[2]value", a.getValue());
            } else if ("p[1]".equals(p.getName())) {
                assertNotNull(p.getAttributes());
                assertEquals(0, p.getAttributes().size());
            }

            List<Asm> pParents = p.getParents();
            assertEquals(1, pParents.size());
            assertEquals("p[0]", pParents.get(0).getName());
        }
        Collection<String> anames = asm.getEffectiveAttributeNames();
        assertEquals(3, anames.size());
        assertTrue(anames.containsAll(Arrays.asList("name1", "name2", "p[2]attr")));
        assertNotNull(asm.getEffectiveCore());
        assertEquals(core.getId(), asm.getEffectiveCore().getId());
    }
}
