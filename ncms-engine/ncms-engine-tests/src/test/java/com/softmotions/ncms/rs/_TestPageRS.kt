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
 * @author Adamansky Anton (adamansky@softmotions.com)
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
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(hasNonNull("id"))
                assertEquals(pageName, path("name").asText())
                assertEquals("page", path("type").asText(null))
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
            assertEquals(page.path("name"), res.path("name"))
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
                        assertEquals(pname, path("label").asText())
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

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete"))
    fun testPageEditGet() {
        with(createPage()) {
            val pid = path("id").asLong()
            val pname = path("name").asText()

            with(GET("/edit/$pid")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(hasNonNull("guid"))
                    assertFalse(hasNonNull("core"))
                    assertFalse(hasNonNull("template"))
                    assertEquals(pname, path("name").asText())
                    assertFalse(path("published").asBoolean())
                    assertTrue(hasNonNull("attributes"))
                    with(path("attributes")) {
                        assertTrue(isArray)
                        assertEquals(0, size())
                    }
                }
            }

            deletePage(pid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete"))
    fun testPageOwner() {
        with(createPage()) {
            val pid = path("id").asLong()

            with(PUT("/owner/$pid/sadmin")) {
                assertEquals(404, code())
            }
            with(PUT("/owner/$pid/admin")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(hasNonNull("owner"))
                    with(path("owner")) {
                        assertEquals("admin", path("name").asText(null))
                    }
                }
            }

            deletePage(pid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete", "testPageSearch", "testPageGet"))
    fun testPageClone() {
        with(createPage()) {
            val pid = path("id").asLong()
            for (i in 0..1) {
                val pageType = when (i) {
                    0 -> "page"
                    1 -> "page.folder"
                    else -> "page"
                }
                val cloneName = RandomStringUtils.randomAlphanumeric(5)

                val props = mapper.createObjectNode().put("name", cloneName).put("type", pageType).put("id", pid)

                with(PUT("/clone").contentType("application/json").send(mapper.writeValueAsString(props))) {
                    assertEquals(204, code())
                }
                assertEquals("2", GET("/search/count").body())

                var cloneId = 0L
                with(GET("/search?name=$cloneName")) {
                    assertEquals(200, code())
                    val body = body()
                    assertNotNull(body)
                    with(mapper.readTree(body)) {
                        assertTrue(isArray)
                        assertEquals(1, size())
                        with(get(0)) {
                            cloneId = path("id").asLong()
                            assertEquals(cloneName, path("label").asText())
                        }
                    }
                }
                with(GET("/info/$cloneId")) {
                    assertEquals(200, code())
                    val body = body()
                    assertNotNull(body)
                    with(mapper.readTree(body)) {
                        assertEquals(cloneId, path("id").asLong())
                        assertEquals(cloneName, path("name").asText(null))
                        assertEquals(pageType, path("type").asText(null))
                    }
                }
                deletePage(cloneId)
            }
            deletePage(pid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete", "testPageGet"))
    fun testPageUpdateBasic() {
        with(createPage()) {
            val pid = path("id").asLong()
            for (i in 0..1) {
                val pageType = when (i) {
                    0 -> "page"
                    1 -> "page.folder"
                    else -> "page"
                }
                val newName = RandomStringUtils.randomAlphanumeric(5)

                val props = mapper.createObjectNode().put("name", newName).put("type", pageType).put("id", pid)

                with(PUT("/update/basic").contentType("application/json").send(mapper.writeValueAsString(props))) {
                    assertEquals(204, code())
                }

                with(GET("/info/$pid")) {
                    assertEquals(200, code())
                    val body = body()
                    assertNotNull(body)
                    with(mapper.readTree(body)) {
                        assertEquals(pid, path("id").asLong())
                        assertEquals(newName, path("name").asText(null))
                        assertEquals(pageType, path("type").asText(null))
                    }
                }
            }
            deletePage(pid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete"))
    fun testPageLayer() {
        with(GET("/layer")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(0, size())
            }
        }
        with(createPage(null, "page.folder")) {
            val parentId = path("id").asLong()
            val parentName = path("name").asText()
            with(GET("/layer")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    with(get(0)) {
                        assertEquals(parentId, path("id").asLong())
                        assertEquals(parentName, path("label").asText(null))
                    }
                }
            }
            with(GET("/layer/$parentId")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(0, size())
                }
            }
            with(createPage(parentId)) {
                val childId = path("id").asLong()
                val childName = path("name").asText()

                with(GET("/layer/$parentId")) {
                    assertEquals(200, code())
                    val body = body()
                    assertNotNull(body)
                    with(mapper.readTree(body)) {
                        assertTrue(isArray)
                        assertEquals(1, size())
                        with(get(0)) {
                            assertEquals(childId, path("id").asLong())
                            assertEquals(childName, path("label").asText(null))
                        }
                    }
                }
                deletePage(childId)
            }
            deletePage(parentId)
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete", "testPageGet", "testPageUpdateBasic", "testPageLayer"))
    fun testPageMove() {
        with(createPage()) {
            val parentId = path("id").asLong()
            val parentName = path("name").asText()
            with(createPage()) {
                val childId = path("id").asLong()
                val childName = path("name").asText()

                var props = mapper.createObjectNode().put("src", childId).put("tgt", childId)
                with(PUT("/move").contentType("application/json").send(mapper.writeValueAsString(props))) {
                    assertEquals(400, code())
                }
                props = mapper.createObjectNode().put("src", childId).put("tgt", parentId)
                with(PUT("/move").contentType("application/json").send(mapper.writeValueAsString(props))) {
                    assertEquals(400, code())
                }

                props = mapper.createObjectNode().put("name", parentName).put("type", "page.folder").put("id", parentId)
                with(PUT("/update/basic").contentType("application/json").send(mapper.writeValueAsString(props))) {
                    assertEquals(204, code())
                }
                props = mapper.createObjectNode().put("src", childId).put("tgt", parentId)
                with(PUT("/move").contentType("application/json").send(mapper.writeValueAsString(props))) {
                    assertEquals(204, code())
                }
                with(GET("/layer/$parentId")) {
                    assertEquals(200, code())
                    val body = body()
                    assertNotNull(body)
                    with(mapper.readTree(body)) {
                        assertTrue(isArray)
                        assertEquals(1, size())
                        with(get(0)) {
                            assertEquals(childId, path("id").asLong())
                            assertEquals(childName, path("label").asText(null))
                        }
                    }
                }
                deletePage(childId)
            }
            deletePage(parentId)
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete", "testPageEditGet"))
    fun testPageReferers() {
        // lazy test - check length, not values
        with(GET("/referers/orphans")) {
            assertEquals(200, code())
            val orphansEmptyBodyLen = body().length
            with(createPage()) {
                val pid = path("id").asLong()

                with(GET("/referrers/count/$pid")) {
                    assertEquals(200, code())
                    assertEquals("0", body())
                }

                with(GET("/edit/$pid")) {
                    assertEquals(200, code())
                    val body = body()
                    assertNotNull(body)
                    with(mapper.readTree(body)) {
                        val orphanItem = "<li>" +
                                "<a href='/${path("guid").asText()}'>" +
                                path("name").asText() +
                                "</a> " +
                                if (path("published").asBoolean()) {
                                    ""
                                } else {
                                    "(not published)"
                                } +
                                "</li>\n"
                        with(GET("/referers/orphans")) {
                            assertEquals(200, code())
                            val orphansBodyLen = body().length
                            assertEquals(orphansBodyLen, orphansEmptyBodyLen + orphanItem.length)
                        }
                    }
                }

                deletePage(pid)
            }
        }
    }

    @Test(dependsOnMethods = arrayOf("testPageCreate", "testPageDelete"))
    fun testPagePath() {
        with(GET("/path/0")) {
            assertEquals(404, code())
        }
        with(createPage()) {
            val pid = path("id").asLong()
            val pname = path("name").asText()

            with(GET("/edit/$pid")) {
                assertEquals(200, code())
                var body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    val guid = path("guid").asText()
                    with(GET("/path/$pid")) {
                        assertEquals(200, code())
                        body = body()
                        assertNotNull(body)
                        with(mapper.readTree(body)) {
                            assertTrue(path("idPath").isArray)
                            assertEquals(1, path("idPath").size())
                            assertEquals(pid, path("idPath")[0].asLong())
                            assertTrue(path("labelPath").isArray)
                            assertEquals(1, path("labelPath").size())
                            assertEquals(pname, path("labelPath")[0].asText(null))
                            assertTrue(path("guidPath").isArray)
                            assertEquals(1, path("guidPath").size())
                            assertEquals(guid, path("guidPath")[0].asText(null))
                        }
                    }
                }
            }
            deletePage(pid)
        }
    }

    private fun createPage(parent: Long? = null, type: String? = "page"): JsonNode {
        val pageName = RandomStringUtils.randomAlphanumeric(5)
        val props = mapper.createObjectNode().put("name", pageName).put("type", type)
        if (parent != null) {
            props.put("parent", parent)
        }

        with(PUT("/new").contentType("application/json").send(mapper.writeValueAsString(props))) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(hasNonNull("id"))
                assertEquals(pageName, path("name").asText(null))
                assertEquals(type, path("type").asText(null))
                if (parent != null) {
                    assertEquals(parent, path("parent").asLong())
                }

                @Suppress("LABEL_NAME_CLASH")
                return this@with
            }
        }
    }

    private fun deletePage(id: Long) {
        assertEquals(200, DELETE("/$id").code())
    }
}