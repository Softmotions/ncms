package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.*

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
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
        val aname = asm["name"].asText(null)
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
        with(GET("/basic/$aname")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            val res = mapper.readTree(body)
            assertEquals(asm.path("id"), res.path("id"))
            assertEquals(asm.path("name"), res.path("name"))
            assertEquals(asm.path("description"), res.path("description"))
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

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete"))
    fun testAsmRename() {
        with(createAsm()) {
            val aid = path("id").asLong()
            val name = RandomStringUtils.randomAlphabetic(64)

            with(PUT("/rename/$aid/$name")) {
                assertEquals(204, code())
            }

            with(GET("/get/$aid")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                assertEquals(name, res.path("name").asText(null))
            }

            deleteAsm(aid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete"))
    fun testAsmCore() {
        with(createAsm()) {
            val aid = path("id").asLong()
            val core = RandomStringUtils.randomAlphabetic(64)

            val props = mapper.createObjectNode().put("location", core)

            with(PUT("/$aid/core").contentType("application/json").send(mapper.writeValueAsString(props))) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                assertEquals(core, res.path("core").path("location").asText(null))
                assertEquals(core, res.path("effectiveCore").path("location").asText(null))
            }

            with(DELETE("/$aid/core")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                assertNull(res.path("core").asText(null))
                assertNull(res.path("effectiveCore").asText(null))
            }

            deleteAsm(aid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete"))
    fun testAsmParents() {
        with(createAsm()) {
            val pid = path("id").asLong()
            val pname = path("name").asText(null)

            with(createAsm()) {
                val aid = path("id").asLong()

                val parent = mapper.createObjectNode().put("id", pid).put("name", pname)
                val parents = mapper.createArrayNode().add(parent)

                with(PUT("/$aid/parents").contentType("application/json").send(mapper.writeValueAsString(parents))) {
                    assertEquals(200, code())
                    val body = body()
                    assertNotNull(body)
                    val res = mapper.readTree(body)
                    assertEquals("$pid:$pname", res[0].asText(null))
                }

                with(DELETE("/$aid/parents").contentType("application/json").send(mapper.writeValueAsString(parents))) {
                    assertEquals(200, code())
                    val body = body()
                    with(mapper.readTree(body)) {
                        assertTrue(isArray)
                        assertEquals(0, size())
                    }
                }
                deleteAsm(aid)
            }

            deleteAsm(pid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete"))
    fun testAsmAttribute() {
        with(createAsm()) {
            val aid = path("id").asLong()
            val attrName = RandomStringUtils.randomAlphanumeric(12)

            val attribute = mapper.createObjectNode().put("asmId", aid).
                    put("name", attrName).
                    put("type", attrName).
                    put("required", false)

            with(PUT("/$aid/attributes").contentType("application/json").send(mapper.writeValueAsString(attribute))) {
                assertEquals(204, code())
            }

            with(GET("/$aid/attribute/$attrName")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                assertEquals(attribute.path("asmId").asLong(), res.path("asmId").asLong())
                assertEquals(attribute.path("name").asText(), res.path("name").asText(null))
                assertEquals(attribute.path("type").asText(), res.path("type").asText(null))
                assertEquals(attribute.path("required").asBoolean(), res.path("required").asBoolean())
            }

            with(DELETE("/$aid/attribute/$attrName")) {
                assertEquals(204, code())
            }

            with(GET("/$aid/attribute/$attrName")) {
                assertEquals(404, code())
            }

            deleteAsm(aid)
        }
    }

    @Test(dependsOnMethods = arrayOf("testAsmCreate", "testAsmDelete", "testAsmGet", "testAsmAttribute"))
    fun testAsmAttributeReorder() {
        with(createAsm()) {
            val aid = path("id").asLong()
            val attrName = Array<String>(2, { i -> RandomStringUtils.randomAlphanumeric(12) })
            val attribute = Array<ObjectNode>(2, { i ->
                mapper.createObjectNode().put("asmId", aid).
                        put("name", attrName[i]).
                        put("type", attrName[i]).
                        put("required", false)
            })

            for (i in 0..1) {
                with(PUT("/$aid/attributes").contentType("application/json").send(mapper.writeValueAsString(attribute[i]))) {
                    assertEquals(204, code())
                }
            }

            val ordinals = Array(2, { i -> 0L })
            with(GET("/get/$aid")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                for (i in 0..1) {
                    val attr = res.path("effectiveAttributes")[i]
                    assertEquals(attribute[i].path("asmId").asLong(), attr.path("asmId").asLong())
                    assertEquals(attribute[i].path("name").asText(), attr.path("name").asText(null))
                    assertEquals(attribute[i].path("type").asText(), attr.path("type").asText(null))
                    assertEquals(attribute[i].path("required").asBoolean(), attr.path("required").asBoolean())
                    ordinals[i] = attr.path("ordinal").asLong()
                }
            }

            with(PUT("/attributes/reorder/${ordinals[0]}/${ordinals[1]}")) {
                assertEquals(204, code())
            }

            with(GET("/get/$aid")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                val res = mapper.readTree(body)
                for (i in 0..1) {
                    val attr = res.path("effectiveAttributes")[1 - i]
                    assertEquals(attribute[i].path("asmId").asLong(), attr.path("asmId").asLong())
                    assertEquals(attribute[i].path("name").asText(), attr.path("name").asText(null))
                    assertEquals(attribute[i].path("type").asText(), attr.path("type").asText(null))
                    assertEquals(attribute[i].path("required").asBoolean(), attr.path("required").asBoolean())
                    ordinals[i] = attr.path("ordinal").asLong()
                }
            }

            for (i in 0..1) {
                with(DELETE("/$aid/attribute/${attrName[i]}")) {
                    assertEquals(204, code())
                }
                with(GET("/$aid/attribute/${attrName[i]}")) {
                    assertEquals(404, code())
                }
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