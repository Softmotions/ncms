package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kevinsawicki.http.HttpRequest
import com.github.kevinsawicki.http.HttpRequest.*
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
        with(auth(get(R("/select/count")))) {
            assertEquals(200, code())
            assertEquals("0", body())
        }

        with(auth(get(R("/select")))) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(0, size())
            }
        }
    }

    @Test(priority = 1)
    fun testRuleCreate() {
        with(auth(put(R("/rule/${RandomStringUtils.randomAlphanumeric(5)}")))) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(hasNonNull("id"))
            }
        }
        with(auth(get(R("/select/count")))) {
            assertEquals(200, code())
            assertEquals("1", body())
        }
    }

    @Test(priority = 50)
    fun testRuleDelete() {
        with(auth(get(R("/select")))) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                forEach {
                    log.warn(it.toString())
                    assertTrue(it.hasNonNull("id"))
                    with(auth(delete(R("/rule/${it.path("id").asLong()}")))) {
                        assertEquals(200, code())
                    }
                }
            }
        }

        with(auth(get(R("/select/count")))) {
            assertEquals(200, code())
            assertEquals("0", body())
        }
    }

    @Test(priority = 1000)
    fun testFiltersSelect() {
        with(auth(put(R("/rule/${RandomStringUtils.randomAlphanumeric(6)}")))) {
            assertEquals(200, code())
            val cbody = body()
            assertNotNull(cbody)
            with(mapper.readTree(cbody)) {
                assertTrue(isObject)
                assertTrue(hasNonNull("id"))

                val rid = path("id").asLong()
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

                with(auth(delete(R("/rule/$rid")))) {
                    assertEquals(200, code())
                }
            }
        }
    }
}