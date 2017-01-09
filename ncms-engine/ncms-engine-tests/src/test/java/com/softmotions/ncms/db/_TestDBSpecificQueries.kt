package com.softmotions.ncms.db

import com.softmotions.ncms.DbBaseTest
import com.softmotions.ncms.asm.*
import com.softmotions.ncms.media.MediaRS
import com.softmotions.ncms.rds.RefDataStore
import com.softmotions.ncms.user.UserEnvRS
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.ibatis.session.SqlSession
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.util.*

/**
 * Testing of sql queries with merge
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
class _TestDBSpecificQueries(db: String) : DbBaseTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    val mediaRS: MBDAOSupport by lazy {
        MBDAOSupport(MediaRS::class.java, getInstance(SqlSession::class))
    }

    val amContext: MBDAOSupport by lazy {
        MBDAOSupport(AsmAttributeManagerContext::class.java, getInstance(SqlSession::class))
    }

    val ds: MBDAOSupport by lazy {
        MBDAOSupport(AsmRS::class.java, getInstance(SqlSession::class))
    }

    val pageRs: MBDAOSupport by lazy {
        MBDAOSupport(PageRS::class.java, getInstance(SqlSession::class))
    }

    val pageSec: MBDAOSupport by lazy {
        MBDAOSupport(PageSecurityService::class.java, getInstance(SqlSession::class))
    }

    val refDS: MBDAOSupport by lazy {
        MBDAOSupport(RefDataStore::class.java, getInstance(SqlSession::class))
    }

    val userER: MBDAOSupport by lazy {
        MBDAOSupport(UserEnvRS::class.java, getInstance(SqlSession::class))
    }

    val aEL: MBDAOSupport by lazy {
        MBDAOSupport(AsmEventsListener::class.java, getInstance(SqlSession::class))
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
    fun testMergeQueries() {

        // test only query syntax, not data!

        val adao = getInstance(AsmDAO::class)
        val asm = Asm()
        asm.name = "foo"
        adao.asmInsert(asm)

        // test query upsertAttirbute@AsmDAO
        for (i in 0..1) {
            Assert.assertEquals(1, adao.update("upsertAttribute",
                    "asmId", 1L,
                    "name", "test",
                    "label", "test",
                    "type", "test",
                    "options", "test",
                    "required", false,
                    "value", "test",
                    "largeValue", "test"))
        }

        // test query mergeFileDependencies@AsmAttributeManagerContext
        mediaRS.insert("insertEntity",
                "folder", "test",
                "name", "test",
                "status", 0,
                "content_type", "test",
                "put_content_type", "test",
                "content_length", 0,
                "owner", "test",
                "description", "test",
                "tags", "test",
                "meta", "test",
                "system", false)
        for (i in 0..1) {
            val res = when (i) {
                0 -> 1
                1 -> 0
                else -> 0
            }
            Assert.assertEquals(res, amContext.update("mergeFileDependencies",
                    "list", listOf(listOf(0L, 1L, 1L))))
        }

        // test query mergePageDependencies@AsmAttributeManagerContext
        for (i in 0..1) {
            val res = when (i) {
                0 -> 1
                1 -> 0
                else -> 0
            }
            Assert.assertEquals(res, amContext.update("mergePageDependencies",
                    "list", listOf(listOf(0L, 1L, "foo"))))
        }
        Assert.assertEquals(0, amContext.update("mergePageDependencies",
                "list", listOf(listOf(0L, 1L, "bar"))))

        // test query setAsmRefData@AsmDAO
        for (i in 0..1) {
            val res = when (i) {
                0 -> 1
                1 -> 0
                else -> 0
            }
            Assert.assertEquals(res, adao.update("setAsmRefData",
                    "id", 1L,
                    "type", "test",
                    "svalue", "test",
                    "ivalue", 1L))
        }

        // test query mergeNewPage@PageRS
        val aclId = pageSec.selectOne<Long>("newAclId")
        for (i in 0..1) {
            Assert.assertEquals(1, pageRs.update("mergeNewPage",
                    "guid", "test",
                    "name", "test",
                    "type", "test",
                    "nav_parent_id", 1L,
                    "lang", "RU",
                    "nav_cached_path", "test",
                    "options", "",
                    "user", "test",
                    "description", "test",
                    "recursive_acl_id", aclId))
        }

        // test query updateAclUserRights@PageSecurityService
        for (i in 0..1) {
            pageSec.update("updateAclUserRights",
                    "acl", aclId,
                    "user", "test",
                    "rights", "tes$i")
            Assert.assertEquals("tes$i", pageSec.selectOne("selectUserRightsByAcl",
                    "acl", aclId,
                    "user", "test"))
        }

        // test query updateChildRecursiveAcl2@PageSecurityService
        val asm2 = Asm()
        asm2.name = "barbar"
        adao.asmInsert(asm2)
        pageRs.update("movePage",
                "id", asm2.id,
                "nav_parent_id", asm.id,
                "lang", "RU",
                "nav_cached_path", "test")
        pageSec.update("setRecursiveAcl",
                "acl", aclId,
                "pid", asm2.id)
        val aclId2 = pageSec.selectOne<Long>("newAclId")
        for (i in 0..1) {
            pageSec.update("updateChildRecursiveAcl2",
                    "acl", aclId2,
                    "user", "test",
                    "rights", "Tes$i",
                    "nav_path", "test")
            Assert.assertEquals("Tes$i", pageSec.selectOne("selectUserRightsByAcl",
                    "acl", aclId,
                    "user", "test"))
        }

        // test query saveData@RefDataStore
        for (i in 0..1) {
            Assert.assertEquals(1, refDS.update("saveData",
                    "ref", "test",
                    "data", null,
                    "content_type", "test"))
        }
    }

    @Test
    fun testVaringQueries() {
        // test query addSet@UserEnvRS
        for (i in 0..1) {
            val res = when (i) {
                0 -> 1
                1 -> 0
                else -> 0
            }
            Assert.assertEquals(res, userER.update("addSet",
                    "userid", "test",
                    "type", "test1",
                    "vcol", "svalue",
                    "value", "test"))
            Assert.assertEquals(res, userER.update("addSet",
                    "userid", "test",
                    "type", "test2",
                    "vcol", "ivalue",
                    "value", 1L))
        }

        // test query delSet@UserEnvRS
        Assert.assertEquals(1, userER.update("delSet",
                "userid", "test",
                "type", "test1",
                "vcol", "svalue",
                "value", "test"))
        Assert.assertEquals(1, userER.update("delSet",
                "userid", "test",
                "type", "test2",
                "vcol", "ivalue",
                "value", 1L))

    }

    @Test
    fun testSubstringQueries() {
        // test query fixFolderName@MediaRS
        mediaRS.insert("insertEntity",
                "folder", "/foo/bar/",
                "name", "test",
                "status", 0,
                "content_type", "test",
                "put_content_type", "test",
                "content_length", 0,
                "owner", "test",
                "description", "test",
                "tags", "test",
                "meta", "test",
                "system", false)

        var id = mediaRS.selectOne<Long>("selectEntityIdByPath",
                "folder", "/foo/bar/",
                "name", "test")

        var prefixLike = "/foo/%"
        var newPrefix = "/Ѧü/"
        Assert.assertEquals(1, mediaRS.update("fixFolderName",
                "new_prefix", newPrefix,
                "prefix_like", prefixLike,
                "prefix_like_len", prefixLike.length))

        val res = mediaRS.select<Map<String, Any>>("selectEntityPathById",
                "id", id)
        Assert.assertEquals(1, res.size)
        Assert.assertEquals("${newPrefix}bar/", res[0]["folder"]?.toString())

        Assert.assertEquals(1, mediaRS.delete("deleteFile",
                "folder", res[0]["folder"]?.toString(),
                "name", res[0]["name"]?.toString()))

        // test query fixCoreFolderLocation@AsmEventsListener
        val adao = getInstance(AsmDAO::class)
        adao.insert("coreInsert",
                "location", "/bar/foo/",
                "name", "test",
                "template_engine", "test")
        var res1 = adao.select<AsmCore>("selectAsmCore",
                "location", "/bar/foo/",
                "name", "test")
        Assert.assertEquals(1, res1.size)
        id = res1[0].id

        prefixLike = "/bar/%"
        newPrefix = "/üѦ/"
        Assert.assertEquals(1, aEL.update("fixCoreFolderLocation",
                "new_prefix", newPrefix,
                "prefix_like", prefixLike,
                "prefix_like_len", prefixLike.length))

        res1 = adao.select<AsmCore>("selectAsmCore",
                "id", id)
        Assert.assertEquals(1, res.size)
        Assert.assertEquals("${newPrefix}foo/", res1[0].location)

        Assert.assertEquals(1, adao.delete("coreDelete",
                "id", id))
    }

    @Test
    fun testDateQueries() {
        val adao = getInstance(AsmDAO::class)
        val asm = Asm()
        asm.name = "foobar"
        adao.asmInsert(asm)

        val calendar = Calendar.getInstance()
        Assert.assertEquals(1, adao.update("asmSetEdate",
                "id", asm.id,
                "edate", calendar.time))

        Assert.assertEquals(1, adao.update("setAsmRefData",
                "id", asm.id,
                "type", "bar",
                "svalue", "svalue",
                "ivalue", 1L))

        val res = adao.select<Map<String, Any>>("selectAsmEventRef",
                "type", "bar",
                "edateLTYear", 3000,
                "edateGTYear", 2000,
                "edateLTDay", 365,
                "edateGTDay", 1)
        Assert.assertEquals(1, res.size)
        Assert.assertEquals(asm.id, res[0]["asm_id"])
        Assert.assertEquals("svalue", res[0]["svalue"])
    }
}
