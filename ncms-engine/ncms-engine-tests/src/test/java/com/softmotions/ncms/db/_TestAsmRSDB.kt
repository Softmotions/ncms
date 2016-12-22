package com.softmotions.ncms.db

import com.softmotions.ncms.DbBaseTest
import com.softmotions.ncms.asm.Asm
import com.softmotions.ncms.asm.AsmAttribute
import com.softmotions.ncms.asm.AsmDAO
import com.softmotions.ncms.asm.AsmRS
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.ibatis.session.SqlSession
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Testing of sql queries for AsmRS
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class _TestAsmRSDB(db: String) : DbBaseTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    val ds: MBDAOSupport by lazy {
        MBDAOSupport(AsmRS::class.java, getInstance(SqlSession::class))
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
    fun testBasicAsmRSOperations() {

        val adao = getInstance(AsmDAO::class)
        val asm = Asm()
        asm.name = "foo"
        asm.type = "bar"

        // test query "select"
        var asmList = ds.select<Map<String, Any>>("select",
                "name", "foo")
        Assert.assertEquals(0, asmList.size)
        Assert.assertEquals(1, adao.asmInsert(asm))

        asmList = ds.select<Map<String, Any>>("select",
                "name", "foo")
        Assert.assertEquals(1, asmList.size)

        // test query "count"
        val count = ds.selectOne<Int>("count",
                "name", "foo")
        Assert.assertEquals(1, count)

        // test query "selectAttrByName"
        val attr1 = AsmAttribute("name1", "type1", "val1")
        Assert.assertEquals(1, adao.asmSetAttribute(asm, attr1))

        var attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name1")
        Assert.assertEquals(1, attr2.size)
        Assert.assertEquals("type1", attr2[0].type)
        Assert.assertEquals("val1", attr2[0].value)

        // test query "prevAttrID"
        Assert.assertEquals(1L, ds.selectOne("prevAttrID")?:0L)

        // test query "renameAttribute"
        Assert.assertEquals(1, ds.update("renameAttribute",
                "asm_id", asm.id,
                "old_name", "name1",
                "new_name", "name2"))
        attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name1")
        Assert.assertEquals(0, attr2.size)
        attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name2")
        Assert.assertEquals(1, attr2.size)
        Assert.assertEquals("type1", attr2[0].type)
        Assert.assertEquals("val1", attr2[0].value)

        // test query "deleteAttribute"
        Assert.assertEquals(1, ds.delete("deleteAttribute",
                "asm_id", asm.id,
                "name", "name2"))
        attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name2")
        Assert.assertEquals(0, attr2.size)

        // test query "insertArrtibute"
        Assert.assertEquals(1, ds.insert("insertAttribute",
                "asmId", asm.id,
                "name", "name3",
                "type", "type3",
                "value", "val3",
                "required", false))
        attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name3")
        Assert.assertEquals(1, attr2.size)
        Assert.assertEquals("type3", attr2[0].type)
        Assert.assertEquals("val3", attr2[0].value)
        Assert.assertEquals(false, attr2[0].isRequired)
        Assert.assertEquals(2L, ds.selectOne("prevAttrID")?:0L)

        // test query "updateArrtibute"
        Assert.assertEquals(1, ds.insert("updateAttribute",
                "asmId", asm.id,
                "name", "name3",
                "type", "type4",
                "value", "val4",
                "required", true))
        attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name3")
        Assert.assertEquals(1, attr2.size)
        Assert.assertEquals("type4", attr2[0].type)
        Assert.assertEquals("val4", attr2[0].value)
        Assert.assertEquals(true, attr2[0].isRequired)

        // test query "selectAsmIdByOrdinal"
        var asmIdList = ds.select<Long>("selectAsmIdByOrdinal",
                "ordinal", 2L)
        Assert.assertEquals(1, asmIdList.size)
        Assert.assertEquals(asm.id, asmIdList[0])

        // test query "exchangeAttributesOrdinal"
        Assert.assertEquals(1, ds.insert("insertAttribute",
                "asmId", asm.id,
                "name", "name4",
                "type", "type4",
                "value", "val4",
                "required", false))
        Assert.assertEquals(2, ds.update("exchangeAttributesOrdinal",
                "asmId", asm.id,
                "ordinal1", 2L,
                "ordinal2", 3L))
        attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name3")
        Assert.assertEquals(3L, attr2[0].ordinal)
        attr2 = ds.select<AsmAttribute>("selectAttrByName",
                "asm_id", asm.id,
                "name", "name4")
        Assert.assertEquals(2L, attr2[0].ordinal)

        // test query "selectNotEmptyChilds" (select childs with non-empty child list)
        /*
         Test assemblies inheritance:

             asm
             /\
            /  \
         p[0]  p[1]
           \    /
             \/
            p[2]
         */
        val childsArr = arrayOf(Asm("p[0]"), Asm("p[1]"), Asm("p[2]"))
        for (a in childsArr) {
            adao.asmInsert(a)
        }
        adao.asmSetParent(childsArr[0], asm)
        adao.asmSetParent(childsArr[1], asm)
        adao.asmSetParent(childsArr[2], childsArr[0])
        adao.asmSetParent(childsArr[2], childsArr[1])
        asmIdList = ds.select<Long>("selectNotEmptyChilds",
                "parent_id", asm.id)
        Assert.assertEquals(2, asmIdList.size)

        // test query "renameAttributeChilds"
        adao.asmSetAttribute(childsArr[0], AsmAttribute("attr", "c[0]type", "c[0]value"))
        adao.asmSetAttribute(childsArr[1], AsmAttribute("attr", "c[1]type", "c[1]value"))
        adao.asmSetAttribute(childsArr[1], AsmAttribute("attr2", "c[1]type", "c[1]value"))
        Assert.assertEquals(2, ds.update("renameAttributeChilds",
                "parent_id", asm.id,
                "old_name", "attr",
                "new_name", "attr1"))

        // test query "deleteAttributeFromChilds"
        Assert.assertEquals(2, ds.delete("deleteAttributeFromChilds",
                "parent_id", asm.id,
                "name", "attr1"))

        // test query "updateAssemblyProps"
        Assert.assertEquals(1, ds.update("updateAssemblyProps",
                "id", childsArr[2].id,
                "published", true,
                "description", "test",
                "template", true))
        asmList = ds.select<Map<String, Any>>("select",
                "name", "p[2]")
        val pub = asmList[0]["published"].toString()
        Assert.assertEquals(true, pub == "1" || pub == "true")
        Assert.assertEquals("test", asmList[0]["description"])

        // test query "selectAsmTParents" (test query only! not result)
        ds.select<Map<String, Any>>("selectAsmTParents",
                "id", asm.id)
    }


}