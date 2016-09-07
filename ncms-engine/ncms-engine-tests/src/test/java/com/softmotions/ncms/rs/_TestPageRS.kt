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
    fun testPageGet() {
        assertEquals(404, GET("/get/0").code())

        val page = createPage()
        val pid = page["id"].asLong()
        with(GET("/rights/$pid")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            assertEquals("ownd", body)
        }
        with(GET("/info/$pid")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            val res = mapper.readTree(body)
            assertEquals(page.path("id"), res.path("id"))
            assertEquals(page.path("label"), res.path("name"))
            assertEquals(page.path("type"), res.path("type"))
            assertEquals("ownd", res.path("accessMask").asText(null))
            assertFalse(res.path("published").asBoolean())
            assertFalse(res.path("template").asBoolean())
            assertEquals("admin", res.path("owner").path("name").asText(null))
            assertEquals("admin", res.path("muser").path("name").asText(null))
        }
        deletePage(pid)
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

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete"))
    fun testPageAcl() {
        with(createPage()) {
            val pid = path("id").asLong()

            with(GET("/acl/$pid")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(0, size())
                }
            }

            with(PUT("/acl/$pid/admin?recursive=false")) {
                assertEquals(204, code())
            }
            with(GET("/acl/$pid?recursive=false")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    with(get(0)) {
                        assertEquals("admin", path("user").asText(null))
                        assertEquals("ownd", path("rights").asText(null))
                        assertFalse(path("recursive").asBoolean())
                    }
                }
            }
            with(GET("/acl/$pid?recursive=true")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(0, size())
                }
            }

            with(PUT("/acl/$pid/admin?recursive=true")) {
                assertEquals(204, code())
            }
            for (i in listOf(false, true)) {
                with(GET("/acl/$pid?recursive=$i")) {
                    assertEquals(200, code())
                    with(mapper.readTree(body())) {
                        assertTrue(isArray)
                        assertEquals(1, size())
                        with(get(0)) {
                            assertEquals(i, path("recursive").asBoolean())
                            assertEquals("admin", path("user").asText(null))

                            if (i) {
                                assertEquals("", path("rights").asText(null))
                            } else {
                                assertEquals("ownd", path("rights").asText(null))
                            }
                        }
                    }
                }
            }

            with(POST("/acl/$pid/admin?recursive=true&add=true&rights=w")) {
                assertEquals(204, code())
            }
            with(POST("/acl/$pid/admin?recursive=false&add=false&rights=o")) {
                assertEquals(204, code())
            }
            for (i in listOf(false, true)) {
                with(GET("/acl/$pid?recursive=$i")) {
                    assertEquals(200, code())
                    with(mapper.readTree(body())) {
                        assertTrue(isArray)
                        assertEquals(1, size())
                        with(get(0)) {
                            assertEquals(i, path("recursive").asBoolean())
                            assertEquals("admin", path("user").asText(null))

                            if (i) {
                                assertEquals("w", path("rights").asText(null))
                            } else {
                                assertEquals("wnd", path("rights").asText(null))
                            }
                        }
                    }
                }
            }

            with(DELETE("/acl/$pid/admin?recursive=true")) {
                assertEquals(204, code())
            }
            with(GET("/acl/$pid?recursive=false")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    with(get(0)) {
                        assertEquals("admin", path("user").asText(null))
                        assertEquals("wnd", path("rights").asText(null))
                        assertFalse(path("recursive").asBoolean())
                    }
                }
            }
            with(GET("/acl/$pid?recursive=true")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(0, size())
                }
            }
            with(DELETE("/acl/$pid/admin?recursive=false")) {
                assertEquals(204, code())
            }
            with(GET("/acl/$pid?recursive=false")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(0, size())
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