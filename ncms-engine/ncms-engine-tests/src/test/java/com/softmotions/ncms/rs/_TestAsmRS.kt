package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("rs"))
class _TestAsmRS(db: String) : BaseRSTest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    private var mapper = ObjectMapper()

    @BeforeClass
    override fun setup() {
        super.setup()
        mapper = getEnv()?.injector?.getInstance(ObjectMapper::class.java) ?: ObjectMapper()
    }

    @AfterClass
    override fun shutdown() {
        super.shutdown()
    }

    override fun R(resource: String): String = super.R(resource = "/rs/adm/asms$resource")

    @Test()
    fun testAsmSelect() {
        with(GET("/select/count")) {
            assertEquals(200, code())
            assertEquals("0", body())
        }

        with(GET("/select")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(0, size())
            }
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmSelect"))
    fun testAsmCreate() {
        with(PUT("/new/${RandomStringUtils.randomAlphanumeric(5)}")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(hasNonNull("id"))
                assertTrue(hasNonNull("name"))
                assertFalse(path("template").asBoolean())
                assertFalse(path("published").asBoolean())
            }
        }

        with(GET("/select/count")) {
            assertEquals(200, code())
            assertEquals("1", body())
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate"))
    fun testAsmDelete() {
        with(GET("/select")) {
            assertEquals(200, code())

            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                forEach {
                    assertTrue(it.hasNonNull("id"))
                    assertEquals(204, DELETE("/delete/${it.path("id").asLong()}").code())
                }
            }
        }

        with(GET("/select/count")) {
            assertEquals(200, code())
            assertEquals("0", body())
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete"))
    fun testAsmGet() {
        assertEquals(404, GET("/get/0").code())

        val asm = createAsm()
        val aid = asm["id"].asLong()
        with(GET("/get/$aid")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            val res = mapper.readTree(body)
            assertEquals(asm.path("id"), res.path("id"))
            assertEquals(asm.path("name"), res.path("name"))
            assertEquals(asm.path("template"), res.path("template"))
            assertEquals(asm.path("published"), res.path("published"))
        }
        deleteAsm(aid)
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete"))
    fun testAsmSearch() {
        with(createAsm()) {
            val aid = path("id").asLong()
            val rname = path("name").asText("")

            assertEquals("1", GET("/select/count").body())
            assertEquals("0", GET("/select/count?stext=A$rname").body())
            assertEquals("1", GET("/select/count?stext=$rname").body())
            with(GET("/select?stext=$rname")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    with(get(0)) {
                        assertEquals(aid, path("id").asLong())
                        assertEquals(rname, path("name").asText())
                    }
                }
            }
            deleteAsm(aid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete"))
    fun testAsmUpdate() {
        with(createAsm()) {
            val aid = path("id").asLong()

            assertFalse(hasNonNull("description"))
            assertFalse(hasNonNull("templateMode"))
            assertFalse(path("published").asBoolean())

            var props = mapper.createObjectNode().put("published", true).put("templateMode", "page")

            with(PUT("/$aid/props").contentType("application/json").send(mapper.writeValueAsString(props))) {
                assertEquals(204, code())
            }

            with(GET("/get/$aid")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                assertEquals("page", res.path("templateMode").asText(null))
                assertTrue(res.path("published").asBoolean(false))
            }

            props = mapper.createObjectNode().put("description", RandomStringUtils.randomAlphabetic(64))

            with(PUT("/$aid/props").contentType("application/json").send(mapper.writeValueAsString(props))) {
                assertEquals(204, code())
            }

            with(GET("/get/$aid")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                assertEquals("page", res.path("templateMode").asText(null))
                assertTrue(res.path("published").asBoolean(false))
                assertEquals(props["description"].asText(), res.path("description").asText(null))
            }

            deleteAsm(aid)
        }
    }

    private fun createAsm(): JsonNode {
        with(PUT("/new/${RandomStringUtils.randomAlphanumeric(12)}")) {
            assertEquals(200, code())
            with(mapper.readTree(body())) {
                assertTrue(isObject)
                assertTrue(hasNonNull("id"))
                assertTrue(hasNonNull("name"))
                assertFalse(path("template").asBoolean())
                assertFalse(path("published").asBoolean())

                @Suppress("LABEL_NAME_CLASH")
                return this@with
            }
        }
    }

    private fun deleteAsm(id: Long) {
        assertEquals(204, DELETE("/delete/$id").code())
    }
}