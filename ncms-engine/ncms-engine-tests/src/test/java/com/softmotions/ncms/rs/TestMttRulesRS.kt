package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kevinsawicki.http.HttpRequest
import com.github.kevinsawicki.http.HttpRequest.get
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
@Test(groups = arrayOf("rs"))
class TestMttRulesRS : BaseRSTest() {

    private var mapper = ObjectMapper()

    @BeforeClass
    override fun setup() {
        super.setup()

        mapper = getEnv()?.injector?.getInstance(ObjectMapper::class.java) ?: ObjectMapper()
    }

    override fun R(resource: String): String = super.R(resource = "/rs/adm/mtt/rules$resource")

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

    @Test(dependsOnMethods = arrayOf("testRulesSelect"))
    fun testRuleCreate() {
        with(PUT("/rule/${RandomStringUtils.randomAlphanumeric(5)}")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(hasNonNull("id"))
            }
        }

        with(GET("/select/count")) {
            assertEquals(200, code())
            assertEquals("1", body())
        }
    }

    @Test(dependsOnMethods = arrayOf("testRuleCreate"))
    fun testRuleDelete() {
        with(GET("/select")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                forEach {
                    assertTrue(it.hasNonNull("id"))
                    with(DELETE("/rule/${it.path("id").asLong()}")) {
                        assertEquals(200, code())
                    }
                }
            }
        }

        with(GET("/select/count")) {
            assertEquals(200, code())
            assertEquals("0", body())
        }
    }

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testRuleGet() {
        assertEquals(404, GET("/rule/0").code())

        val rule = createRule()
        val rid = rule["id"].asLong()
        with(GET("/rule/$rid")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            assertEquals(rule, mapper.readTree(body))
        }

        assertEquals(200, DELETE("/rule/$rid").code())
    }

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testRuleSearch() {
        with(createRule()) {
            val rid = path("id").asLong()
            val rname = path("name").asText();

            assertEquals("1", GET("/select/count").body())
            assertEquals("0", GET("/select/count?stext=A$rname").body())
            assertEquals("1", GET("/select/count?stext=$rname").body())
            with(GET("/select?stext=$rname")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    with(get(0)) {
                        assertEquals(rid, path("id").asLong())
                        assertEquals(rname, path("name").asText())
                    }
                }
            }
            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testRuleRename() {
    }

// todo: rule tests: rename, update flags, get

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testRuleReorder() {
        var rules = arrayOf(
                createRule(),
                createRule(),
                createRule()
        )

        assertTrue(rules[0]["ordinal"].asLong() < rules[1]["ordinal"].asLong())
        assertTrue(rules[1]["ordinal"].asLong() < rules[2]["ordinal"].asLong())

        with(GET("/select")) {
            assertEquals(200, code())
            with(mapper.readTree(body())) {
                assertTrue(isArray)
                assertEquals(3, size())
                forEachIndexed { index, rule ->
                    assertEquals(rules[index]["id"].asLong(), rule["id"].asLong())
                }
            }
        }

        // actually, nothing changed
        assertEquals(204, POST("/rule/${rules[2]["id"].asLong()}/move/1").code())
        assertEquals(204, POST("/rule/${rules[0]["id"].asLong()}/move/-1").code())
        with(GET("/select")) {
            assertEquals(200, code())
            with(mapper.readTree(body())) {
                assertTrue(isArray)
                assertEquals(3, size())
                forEachIndexed { index, rule ->
                    assertEquals(rules[index]["id"].asLong(), rule["id"].asLong())
                }
            }
        }

        // swap first and second rule
        assertEquals(204, POST("/rule/${rules[0]["id"].asLong()}/move/1").code())
        rules = arrayOf(rules[1], rules[0], rules[2])
        with(GET("/select")) {
            assertEquals(200, code())
            with(mapper.readTree(body())) {
                assertTrue(isArray)
                assertEquals(3, size())
                forEachIndexed { index, rule ->
                    assertEquals(rules[index]["id"].asLong(), rule["id"].asLong())
                }
            }
        }

        rules.forEach {
            assertEquals(200, DELETE("/rule/${it["id"].asLong()}").code())
        }
    }

    @Test(priority = 1000, dependsOnMethods = arrayOf("testRuleDelete"))
    fun testFiltersSelect() {
        with(createRule()) {
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

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    private fun createRule(): JsonNode {
        with(PUT("/rule/${RandomStringUtils.randomAlphanumeric(12)}")) {
            assertEquals(200, code())
            with(mapper.readTree(body())) {
                assertTrue(isObject)
                assertTrue(hasNonNull("id"))
                assertTrue(hasNonNull("ordinal"))

                @Suppress("LABEL_NAME_CLASH")
                return this@with;
            }
        }
    }
}