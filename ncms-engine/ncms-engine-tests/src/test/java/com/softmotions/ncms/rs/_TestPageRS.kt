package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.*

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("rs"))
class _TestPageRS(db: String) : BaseRSTest(db) {

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

    override fun R(resource: String): String = super.R(resource = "/rs/adm/pages$resource")

    @Test()
    fun testPageSearchBasic() {
        with(GET("/search/count")) {
            assertEquals(200, code())
            assertEquals("0", body())
        }

        with(GET("/search")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(0, size())
            }
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageSearchBasic"))
    fun testPageCreate() {
        val pageName = RandomStringUtils.randomAlphanumeric(5)
        val props = mapper.createObjectNode().put("name", pageName).put("type", "page")

        with(PUT("/new").contentType("application/json").send(mapper.writeValueAsString(props))) {
            assertEquals(204, code())
        }

        with(GET("/search/count")) {
            assertEquals(200, code())
            assertEquals("1", body())
        }

        with(GET("/search")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                with(get(0)) {
                    assertTrue(hasNonNull("id"))
                    assertEquals(pageName, path("label").asText())
                    assertFalse(path("published").asBoolean())
                }
            }
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate"))
    fun testPageDelete() {
        with(GET("/search")) {
            assertEquals(200, code())

            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                forEach {
                    assertTrue(it.hasNonNull("id"))
                    assertEquals(200, DELETE("/${it.path("id").asLong()}").code())
                }
            }
        }

        with(GET("/search/count")) {
            assertEquals(200, code())
            assertEquals("0", body())
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete"))
    fun testPageSearch() {
        with(createPage()) {
            val pid = path("id").asLong()
            val pname = path("name").asText("")

            assertEquals("1", GET("/search/count").body())
            assertEquals("0", GET("/search/count?name=A$pname").body())
            assertEquals("1", GET("/search/count?name=$pname").body())
            with(GET("/search?name=$pname")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    with(get(0)) {
                        assertEquals(pid, path("id").asLong())
                        assertEquals(pname, path("name").asText())
                    }
                }
            }
            deletePage(pid)
        }
    }

    private fun createPage(): JsonNode {
        val pageName = RandomStringUtils.randomAlphanumeric(5)
        val props = mapper.createObjectNode().put("name", pageName).put("type", "page")

        with(PUT("/new").contentType("application/json").send(mapper.writeValueAsString(props))) {
            assertEquals(204, code())
        }

        with(GET("/search?name=$pageName")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(1, size())
                with(get(0)) {
                    assertTrue(hasNonNull("id"))
                    assertEquals(pageName, path("label").asText())
                    assertFalse(path("published").asBoolean())

                    @Suppress("LABEL_NAME_CLASH")
                    return this@with
                }
            }
        }
    }

    private fun deletePage(id: Long) {
        assertEquals(200, DELETE("/$id").code())
    }
}