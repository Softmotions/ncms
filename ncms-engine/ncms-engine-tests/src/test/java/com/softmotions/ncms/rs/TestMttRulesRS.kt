package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kevinsawicki.http.HttpRequest
import com.softmotions.commons.JVMResources
import com.softmotions.ncms.WebBaseTest
import com.softmotions.web.security.XMLWSUserDatabase
import com.softmotions.web.security.tomcat.WSUserDatabaseRealm
import com.softmotions.weboot.testing.tomcat.TomcatRunner
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
@Test(groups = arrayOf("rs"))
class TestMttRulesRS : WebBaseTest() {

    @BeforeClass
    fun setup() {
        System.setProperty("liquibase.dropAll", "false")
        if (System.getProperty("ncmstest.ds") == null) {
            System.setProperty("ncmstest.ds",
                    System.getProperty("user.home") + "/.ncms-test.ds")
        }
        System.setProperty("WEBOOT_CFG_LOCATION",
                "com/softmotions/ncms/rs/cfg/test-ncms-rs-conf.xml")
        setupWeb()
        runner!!.start()
    }

    override fun configureTomcatRunner(b: TomcatRunner.Builder) {
        super.configureTomcatRunner(b)
        val wsdb = XMLWSUserDatabase("WSUserDatabase", "com/softmotions/ncms/rs/cfg/users.xml", false)
        JVMResources.set(wsdb.databaseName, wsdb);
        b.withRealm(WSUserDatabaseRealm(wsdb))
    }

    @AfterClass
    fun shutdown() {
        shutdownWeb()
    }

    override fun R(resource: String): String = super.R(resource = "/rs/adm/mtt/rules$resource")

    private val mapper = ObjectMapper()

    @Test(priority = 0)
    fun testRulesSelect() {
        with(auth(HttpRequest.get(R("/select/count")))) {
            assertEquals(200, code())
            val count = Integer.valueOf(body())

            with(auth(HttpRequest.get(R("/select")))) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(count, size())
                }
            }
        }

    }

    @Test(priority = 1)
    fun testRuleCreate() {
        val rname = RandomStringUtils.randomAlphanumeric(5);
        with(auth(HttpRequest.put(R("/rule/$rname")))) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(hasNonNull("id"))
            }
        }
    }

    @Test(priority = 2)
    fun testFiltersSelect() {
        // todo: create new rule & get rule id by name
//        val rname = RandomStringUtils.randomAlphanumeric(6)
//        assertEquals(200, auth(HttpRequest.put(R("/rule/$rname"))).code())

        val rid = 0
        with(auth(HttpRequest.get(R("/rule/$rid/filters/select/count")))) {
            assertEquals(200, code())
            assertEquals("0", body())
        }

        with(auth(HttpRequest.get(R("/rule/$rid/filters/select")))) {
            assertEquals(200, code())
            val body = body();
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(0, size())
            }
        }
    }
}