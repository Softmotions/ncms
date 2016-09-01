package com.softmotions.ncms.db

import com.softmotions.ncms.DbBaseTest
import com.softmotions.ncms.asm.*
import com.softmotions.ncms.asm.am.AsmAttributeManagerContext
import com.softmotions.ncms.media.MediaRS
import com.softmotions.ncms.rds.RefDataStore
import com.softmotions.ncms.user.UserEnvRS
import com.softmotions.weboot.mb.MBDAOSupport
import org.apache.ibatis.session.SqlSession
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Testing of sql queries with merge
 *
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
class _TestDBMergeQueries(db: String) : DbBaseTest(db) {

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
        adao.update("upsertAttribute",
                "asmId" , 1L,
                "name" , "test",
                "label" , "test",
                "type" , "test",
                "options" , "test",
                "required" , false,
                "value" , "test",
                "largeValue" , "test")

        // test query mergeFileDependencies@AsmAttributeManagerContext
        mediaRS.insert("insertEntity",
                "folder" , "test",
                "name" , "test",
                "status" , 0,
                "content_type" , "test",
                "put_content_type" , "test",
                "content_length" , 0,
                "owner" , "test",
                "description" , "test",
                "tags" , "test",
                "meta" , "test",
                "system" , false)
        amContext.update("mergeFileDependencies", "list", listOf(listOf(1L, 1L)))

        // test query mergePageDependencies@AsmAttributeManagerContext
        // todo: rewrite "and exists"
//        amContext.update("mergePageDependencies", "list", listOf(listOf(1L, 1L)))

        // test query setAsmRefData@AsmDAO
        // todo: add unique constraints?
/*
        adao.update("setAsmRefData",
                "id", 1L,
                "type", "test",
                "svalue", "test",
                "ivalue", 1L)
*/

        // test query mergeNewPage@PageRS
        pageRs.update("mergeNewPage",
                "guid" , "test",
                "name" , "test",
                "type" , "test",
                "nav_parent_id" , 1L,
                "lang" , "RU",
                "nav_cached_path" , "test",
                "options" , "",
                "user" , "test",
                "description" , "test",
                "recursive_acl_id" , 1L)

        // test query updateAclUserRights@PageSecurityService
        // todo: add unique constraints?
/*
        pageSec.update("updateAclUserRights",
                "acl", 1L,
                "user", "test",
                "rights", "test")
*/

        // test query updateChildRecursiveAcl2@PageSecurityService
        // todo: add unique constraints?
/*        pageSec.update("updateChildRecursiveAcl2",
                "acl", 1L,
                "user", "test",
                "rights", "test",
                "nav_path", "test")
*/

        // test query saveData@RefDataStore
        refDS.update("saveData",
                "ref", "test",
                "data", null,
                "content_type", "test")

        // test query addSet@UserEnvRS
        // todo: add unique constraints?
/*        userER.update("addSet",
                "userid", "test",
                "type", "test",
                "vcol", "svalue",
                "value", "test")
*/

    }



}
