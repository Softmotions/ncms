package com.softmotions.ncms.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Module
import com.softmotions.kotlin.InstancesModule
import com.softmotions.ncms.GuiceBaseTest
import com.softmotions.ncms.asm.*
import com.softmotions.weboot.liquibase.WBLiquibaseModule
import com.softmotions.weboot.mb.WBMyBatisModule
import kotlinx.support.jdk7.use
import org.apache.ibatis.exceptions.PersistenceException
import org.testng.Assert.*
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.util.*

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test
open class TestNcmsDB : GuiceBaseTest() {

    protected open fun setup(cfgLocation: String, vararg modules: Module) {
        System.setProperty("liquibase.dropAll", "true");
        val cfg = loadServicesConfiguration(cfgLocation)
        setupGuice(
                InstancesModule(cfg, ObjectMapper()),
                WBMyBatisModule(cfg),
                WBLiquibaseModule(cfg),
                *modules
        )
    }

    @BeforeClass
    fun setup() {
        setup("com/softmotions/ncms/db/cfg/test-ncms-db-conf.xml")
    }

    @AfterClass
    fun shutdown() {
        super.shutdownGuice()
    }

    @Test
    fun testBasicAsmOperations() {

        val adao = getInstance(AsmDAO::class);
        var asm = Asm()
        asm.setName("foo")
        assertEquals(1, adao.asmInsert(asm))

        var asmList = adao.asmSelectAllPlain()
        assertFalse(asmList.isEmpty())

        val asm2 = asmList[0]
        assertNotSame(asm, asm2)
        assertEquals(asm.id, asm2.id)
        assertEquals(asm.name, asm2.name)
        assertEquals(asm.description, asm2.description)

        var hasException = false
        try {
            adao.asmInsert(asm)
        } catch (e: Exception) {
            hasException = true
            assertTrue(e is PersistenceException)
        }
        assertTrue(hasException)

        val attr1 = AsmAttribute("name1", "type1", "val1")
        assertEquals(1, adao.asmSetAttribute(asm, attr1))

        val attr2 = AsmAttribute("name2", "type2", "val2")
        assertEquals(1, adao.asmSetAttribute(asm, attr2))

        var cq: AsmCriteria = adao.newAsmCriteria()
                .orderBy("name").desc()
                .onAsmAttribute()
                .orderBy("type")
                .limit(100)

        asmList = cq.select<Asm>()
        assertEquals(1, asmList.size)

        asm = asmList[0]
        assertEquals(asm.id, asm2.id)
        assertEquals(asm.name, asm2.name)
        assertEquals(asm.description, asm2.description)
        assertNotNull(asm.attributes)

        assertEquals(2, asm.attributes.size)


        var attr = asm.getAttribute("name1")
        assertNotNull(attr1)
        assertEquals(attr1.type, attr!!.type)
        assertEquals(attr1.value, attr.value)

        attr = asm.getAttribute("name2")
        assertNotNull(attr2)
        assertEquals(attr2.type, attr!!.type)
        assertEquals(attr2.value, attr.value)

        hasException = false
        try {
            adao.asmSetAttribute(asm, attr2)
        } catch (e: Exception) {
            hasException = true
            assertTrue(e is PersistenceException)
        }
        assertTrue(hasException)


        //Find by PK
        cq = adao.newAsmCriteria().withPK(asm.id)
        asm = cq.selectOne<Asm>()
        assertNotNull(asm)
        assertEquals(asm.id, asm2.id)
        assertEquals(asm.name, asm2.name)


        //Test Raw JDBC access within MyBatis transaction
        val count = adao.withinTransaction { sess, conn ->
            var ret: Number = 0
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT COUNT(*) FROM ASM_ATTRS").use({ rs ->
                    if (rs.next()) {
                        ret = rs.getObject(1) as Number
                    }
                })
            }
            ret
        }
        assertEquals(2, count.toInt())

        //Insert AsmCore
        var core = AsmCore("file:///some/file", "my first assembly core")
        adao.coreInsert(core)

        //Select by criteria query
        val core2 = adao.newCriteria("location", "file:///some/file").withStatement("selectAsmCore").selectOne<AsmCore>()

        assertNotNull(core2)
        assertEquals(core.id, core2.id)
        assertEquals(core.location, core2.location)


        core = AsmCore("file:///some/file2", "the second assembly core")
        adao.coreInsert(core)

        //Update core
        core.name = null
        core.location = null
        core.templateEngine = "freemarker"
        assertEquals(1, adao.coreUpdate(core))

        //Test attachment of core
        core = adao.newCriteria("name", "the second assembly core").withStatement("selectAsmCore").selectOne<AsmCore>()
        assertNotNull(core)
        assertEquals("freemarker", core.templateEngine)

        asm = adao.newAsmCriteria().withParam("name", "foo").selectOne<Asm>()
        assertNotNull(asm)

        asm.core = core
        assertEquals(1, adao.asmUpdate(asm))

        asm = adao.newAsmCriteria().withParam("name", "foo").selectOne<Asm>()
        assertNotNull(asm)
        assertNotNull(asm.core)
        assertEquals(core.id, asm.core!!.id)
        assertEquals(core.location, asm.core!!.location)

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
        val parentsArr = arrayOf(Asm("p[0]"), Asm("p[1]"), Asm("p[2]"))
        for (a in parentsArr) {
            adao.asmInsert(a)
        }
        parentsArr[0].core = core
        adao.asmUpdate(parentsArr[0])
        adao.asmSetParent(parentsArr[1], parentsArr[0])
        adao.asmSetParent(parentsArr[2], parentsArr[0])
        adao.asmSetParent(asm, parentsArr[1])
        adao.asmSetParent(asm, parentsArr[2])
        adao.asmSetAttribute(parentsArr[2], AsmAttribute("p[2]attr", "p[2]type", "p[2]value"))

        asm = adao.newAsmCriteria().withPK(asm.id).selectOne<Asm>()
        assertNotNull(asm)

        //Toggle lazy loading of parents
        val parents = asm.parents
        assertEquals(2, parents.size)

        var i = 0
        val l = parents.size
        while (i < l) {
            val p = parents[i]
            assertTrue("p[1]" == p.name || "p[2]" == p.name)
            if ("p[2]" == p.name) {
                assertNotNull(p.attributes)
                assertEquals(1, p.attributes.size)
                val a = p.attributes.iterator().next()
                assertEquals("p[2]attr", a.name)
                assertEquals("p[2]type", a.type)
                assertEquals("p[2]value", a.value)
            } else if ("p[1]" == p.name) {
                assertNotNull(p.attributes)
                assertEquals(0, p.attributes.size)
            }

            val pParents = p.parents
            assertEquals(1, pParents.size)
            assertEquals("p[0]", pParents[0].name)
            ++i
        }
        val anames = asm.effectiveAttributeNames
        assertEquals(3, anames.size)
        assertTrue(anames.containsAll(Arrays.asList("name1", "name2", "p[2]attr")))
        assertNotNull(asm.effectiveCore)
        assertEquals(core.id, asm.effectiveCore!!.id)

    }
}