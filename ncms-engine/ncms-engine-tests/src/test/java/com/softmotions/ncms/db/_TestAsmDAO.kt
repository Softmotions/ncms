package com.softmotions.ncms.db

import com.softmotions.ncms.DbBaseTest
import com.softmotions.ncms.asm.*
import org.apache.ibatis.exceptions.PersistenceException
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.util.*

@Test
class _TestAsmDAO
constructor(db: String) : DbBaseTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    @BeforeClass
    fun setup() {
        super.setup("com/softmotions/ncms/db/cfg/test-ncms-db-conf.xml")
    }

    @AfterClass
    override fun shutdown() {
        super.shutdown()
    }

    @Test
    fun testBasicAsmOperations() {

        val adao = getInstance(AsmDAO::class);
        var asm = Asm()
        asm.setName("foo")
        Assert.assertEquals(1, adao.asmInsert(asm))

        var asmList = adao.asmSelectAllPlain()
        Assert.assertFalse(asmList.isEmpty())

        val asm2 = asmList[0]
        Assert.assertNotSame(asm, asm2)
        Assert.assertEquals(asm.id, asm2.id)
        Assert.assertEquals(asm.name, asm2.name)
        Assert.assertEquals(asm.description, asm2.description)

        var hasException = false
        try {
            adao.asmInsert(asm)
        } catch (e: Exception) {
            hasException = true
            Assert.assertTrue(e is PersistenceException)
        }
        Assert.assertTrue(hasException)

        val attr1 = AsmAttribute("name1", "type1", "val1")
        Assert.assertEquals(1, adao.asmSetAttribute(asm, attr1))

        val attr2 = AsmAttribute("name2", "type2", "val2")
        Assert.assertEquals(1, adao.asmSetAttribute(asm, attr2))


        adao.asmUpsertAttribute(attr2);

        var cq: AsmCriteria = adao.newAsmCriteria()
                .orderBy("name").desc()
                .onAsmAttribute()
                .orderBy("type")
                .limit(100)

        asmList = cq.select<Asm>()
        Assert.assertEquals(1, asmList.size)

        asm = asmList[0]
        Assert.assertEquals(asm.id, asm2.id)
        Assert.assertEquals(asm.name, asm2.name)
        Assert.assertEquals(asm.description, asm2.description)
        Assert.assertNotNull(asm.attributes)

        Assert.assertEquals(2, asm.attributes!!.size)


        var attr = asm.getAttribute("name1")
        Assert.assertNotNull(attr1)
        Assert.assertEquals(attr1.type, attr!!.type)
        Assert.assertEquals(attr1.value, attr.value)

        attr = asm.getAttribute("name2")
        Assert.assertNotNull(attr2)
        Assert.assertEquals(attr2.type, attr!!.type)
        Assert.assertEquals(attr2.value, attr.value)

        hasException = false
        try {
            adao.asmSetAttribute(asm, attr2)
        } catch (e: Exception) {
            hasException = true
            Assert.assertTrue(e is PersistenceException)
        }
        Assert.assertTrue(hasException)


        //Find by PK
        cq = adao.newAsmCriteria().withPK(asm.id)
        asm = cq.selectOne<Asm>()
        Assert.assertNotNull(asm)
        Assert.assertEquals(asm.id, asm2.id)
        Assert.assertEquals(asm.name, asm2.name)


        //Test Raw JDBC access within MyBatis transaction
        val count = adao.withinTransaction { sess, conn ->
            var ret: Number = 0
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT COUNT(*) FROM asm_attrs").use({ rs ->
                    if (rs.next()) {
                        ret = rs.getObject(1) as Number
                    }
                })
            }
            ret
        }
        Assert.assertEquals(2, count?.toInt())

        //Insert AsmCore
        var core = AsmCore("file:///some/file", "my first assembly core")
        adao.coreInsert(core)

        //Select by criteria query
        val core2 = adao.newCriteria("location", "file:///some/file").withStatement("selectAsmCore").selectOne<AsmCore>()

        Assert.assertNotNull(core2)
        Assert.assertEquals(core.id, core2.id)
        Assert.assertEquals(core.location, core2.location)


        core = AsmCore("file:///some/file2", "the second assembly core")
        adao.coreInsert(core)

        //Update core
        core.name = null
        core.location = null
        core.templateEngine = "freemarker"
        Assert.assertEquals(1, adao.coreUpdate(core))

        //Test attachment of core
        core = adao.newCriteria("name", "the second assembly core").withStatement("selectAsmCore").selectOne<AsmCore>()
        Assert.assertNotNull(core)
        Assert.assertEquals("freemarker", core.templateEngine)

        asm = adao.newAsmCriteria().withParam("name", "foo").selectOne<Asm>()
        Assert.assertNotNull(asm)

        asm.core = core
        Assert.assertEquals(1, adao.asmUpdate(asm))

        asm = adao.newAsmCriteria().withParam("name", "foo").selectOne<Asm>()
        Assert.assertNotNull(asm)
        Assert.assertNotNull(asm.core)
        Assert.assertEquals(core.id, asm.core!!.id)
        Assert.assertEquals(core.location, asm.core!!.location)

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
        Assert.assertNotNull(asm)

        //Toggle lazy loading of parents
        val parents = asm.parents
        Assert.assertEquals(2, parents!!.size)

        var i = 0
        val l = parents.size
        while (i < l) {
            val p = parents[i]
            Assert.assertTrue("p[1]" == p.name || "p[2]" == p.name)
            if ("p[2]" == p.name) {
                Assert.assertNotNull(p.attributes)
                Assert.assertEquals(1, p.attributes!!.size)
                val a = p.attributes!!.iterator().next()
                Assert.assertEquals("p[2]attr", a.name)
                Assert.assertEquals("p[2]type", a.type)
                Assert.assertEquals("p[2]value", a.value)
            } else if ("p[1]" == p.name) {
                Assert.assertNotNull(p.attributes)
                Assert.assertEquals(0, p.attributes!!.size)
            }

            val pParents = p.parents
            Assert.assertEquals(1, pParents!!.size)
            Assert.assertEquals("p[0]", pParents[0].name)
            ++i
        }
        val anames = asm.effectiveAttributeNames
        Assert.assertEquals(3, anames.size)
        Assert.assertTrue(anames.containsAll(Arrays.asList("name1", "name2", "p[2]attr")))
        Assert.assertNotNull(asm.effectiveCore)
        Assert.assertEquals(core.id, asm.effectiveCore!!.id)


    }
}