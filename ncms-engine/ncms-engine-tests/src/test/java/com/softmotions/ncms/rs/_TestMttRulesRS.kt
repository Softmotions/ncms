package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.*

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Test(groups = arrayOf("rs"))
class _TestMttRulesRS(db: String) : BaseRSTest(db) {

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

    override fun R(resource: String): String = super.R(resource = "/rs/adm/mtt/rules$resource")

    @Test()
    fun testRulesSelect() {
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

    @Test(dependsOnMethods = arrayOf("testRulesSelect"))
    fun testRuleCreate() {
        with(PUT("/rule/${RandomStringUtils.randomAlphanumeric(5)}")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(hasNonNull("id"))
                assertTrue(hasNonNull("ordinal"))
                assertTrue(path("enabled").asBoolean())
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
                    assertEquals(200, DELETE("/rule/${it.path("id").asLong()}").code())
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

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testRuleUpdate() {
        with(createRule()) {
            val rid = path("id").asLong()

            assertFalse(hasNonNull("description"))
            assertTrue(hasNonNull("flags"))

            var rule = mapper.createObjectNode().put("flags", 8)

            with(POST("/rule/$rid").contentType("application/json").send(mapper.writeValueAsString(rule))) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertEquals(8, path("flags").asLong())
                    assertFalse(hasNonNull("description"))
                }
            }

            rule = mapper.createObjectNode().put("description", RandomStringUtils.randomAlphabetic(64))

            with(POST("/rule/$rid").contentType("application/json").send(mapper.writeValueAsString(rule))) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertEquals(8, path("flags").asLong())
                    assertTrue(hasNonNull("description"))
                    assertEquals(rule["description"].asText(), path("description").asText())
                }
            }

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testRuleEnabling() {
        with(createRule()) {
            val rid = path("id").asLong()

            assertEquals(200, POST("/rule/$rid/enable").code())
            assertTrue(mapper.readTree(GET("/rule/$rid").body()).path("enabled").asBoolean())

            assertEquals(200, POST("/rule/$rid/disable").code())
            assertFalse(mapper.readTree(GET("/rule/$rid").body()).path("enabled").asBoolean())

            assertEquals(200, POST("/rule/$rid/enable").code())
            assertTrue(mapper.readTree(GET("/rule/$rid").body()).path("enabled").asBoolean())

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

// todo: rule tests: rename, update flags, get

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testRuleReorder() {
        var rules = arrayOf(
                createRule(),
                createRule(),
                createRule()
        )

        rules.reverse()

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
        assertEquals(204, POST("/rule/${rules[2]["id"].asLong()}/move/down").code())
        assertEquals(204, POST("/rule/${rules[0]["id"].asLong()}/move/up").code())
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
        assertEquals(204, POST("/rule/${rules[0]["id"].asLong()}/move/down").code())
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

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testFiltersSelect() {
        with(createRule()) {
            val rid = path("id").asLong()
            with(GET("/rule/$rid/filters/select/count")) {
                assertEquals(200, code())
                assertEquals("0", body())
            }

            with(GET("/rule/$rid/filters/select")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(0, size())
                }
            }

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    // todo: filter tests

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testFilterCreate() {
        with(createRule()) {
            val rid = path("id").asLong()
            val fcq = mapper.createObjectNode()
                    .put("type", RandomStringUtils.randomAlphabetic(6).toLowerCase())
                    .put("spec", RandomStringUtils.randomAscii(1024))
                    .put("description", RandomStringUtils.randomAscii(32))

            with(PUT("/rule/$rid/filter").contentType("application/json").send(mapper.writeValueAsString(fcq))) {
                assertEquals(200, code())

                val body = body()
                assertNotNull(body)

                val filter = mapper.readTree(body)
                assertTrue(filter.isObject)
                assertTrue(filter.hasNonNull("id"))
                assertEquals(fcq["type"].asText(), filter["type"].asText(null))
                assertEquals(fcq["spec"].asText(), filter["spec"].asText(null))
                assertEquals(fcq["description"].asText(), filter["description"].asText(null))

                val fid = filter["id"].asLong()
                with(GET("/filter/$fid")) {
                    assertEquals(200, code())
                    assertEquals(filter, mapper.readTree(body()))
                }

                with(GET("/rule/$rid/filters/select/count")) {
                    assertEquals(200, code())
                    assertEquals("1", body())
                }

                with(GET("/rule/$rid/filters/select")) {
                    assertEquals(200, code())
                    with(mapper.readTree(body())) {
                        assertTrue(isArray)
                        assertEquals(1, size())
                        assertEquals(filter, get(0))
                    }
                }

                assertEquals(200, DELETE("/filter/$fid").code())
            }

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testFilterCreate"))
    fun testFilterUpdate() {
        with(createRule()) {
            val rid = path("id").asLong()

            with(createRuleFilter(rid)) {
                val fid = path("id").asLong()

                val fields = arrayOf("type", "spec", "description");
                fields.forEach {
                    val fname = it
                    with(GET("/filter/$fid")) {
                        assertEquals(200, code())

                        val filter = mapper.readTree(body())
                        assertTrue(filter.hasNonNull(fname))
                        val fu = mapper.createObjectNode()
                                .put(fname, RandomStringUtils.randomAscii(filter[fname].asText().length / 2))

                        with(POST("/filter/$fid").contentType("application/json").send(mapper.writeValueAsString(fu))) {
                            assertEquals(200, code())

                            val ufilter = mapper.readTree(body())

                            fields.forEach {
                                when {
                                    it.equals(fname) -> {
                                        assertNotEquals(filter[it].asText(), ufilter[it].asText())
                                        assertEquals(fu[it].asText(), ufilter[it].asText())
                                    }
                                    else -> assertEquals(filter[it].asText(), ufilter[it].asText())
                                }
                            }
                        }
                    }
                }

                assertEquals(200, DELETE("/filter/$fid").code())
            }

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testActionsSelect() {
        with(createRule()) {
            val rid = path("id").asLong()
            with(GET("/rule/$rid/actions/select/count")) {
                assertEquals(200, code())
                assertEquals("0", body())
            }

            with(GET("/rule/$rid/actions/select")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(0, size())
                }
            }

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    // todo: action tests

    @Test(dependsOnMethods = arrayOf("testRuleCreate", "testRuleDelete"))
    fun testActionCreate() {
        with(createRule()) {
            val rid = path("id").asLong()
            val action = mapper.createObjectNode()
                    .put("type", RandomStringUtils.randomAlphabetic(6).toLowerCase())
                    .put("spec", RandomStringUtils.randomAscii(1024))
                    .put("description", RandomStringUtils.randomAscii(32))
            with(PUT("/rule/$rid/action").contentType("application/json").send(mapper.writeValueAsString(action))) {
                assertEquals(200, code())

                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isObject)
                    assertTrue(hasNonNull("id"))
                    assertEquals(action["type"].asText(), path("type").asText(null))
                    assertEquals(action["spec"].asText(), path("spec").asText(null))
                    assertEquals(action["description"].asText(), path("description").asText(null))

                    val aid = path("id").asLong()
                    val req = GET("/action/$aid")
                    assertEquals(200, req.code())
                    @Suppress("LABEL_NAME_CLASH")
                    (assertEquals(this@with, mapper.readTree(req.body())))

                    assertEquals(200, DELETE("/action/$aid").code())
                }
            }

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testActionCreate"))
    fun testActionUpdate() {
        with(createRule()) {
            val rid = path("id").asLong()

            with(createRuleAction(rid)) {
                val aid = path("id").asLong()

                val fields = arrayOf("type", "spec", "description");
                fields.forEach {
                    val fname = it
                    with(GET("/action/$aid")) {
                        assertEquals(200, code())

                        val action = mapper.readTree(body())
                        assertTrue(action.hasNonNull(fname))
                        val au = mapper.createObjectNode()
                                .put(fname, RandomStringUtils.randomAscii(action[fname].asText().length / 2))

                        with(POST("/action/$aid").contentType("application/json").send(mapper.writeValueAsString(au))) {
                            assertEquals(200, code())

                            val uaction = mapper.readTree(body())

                            fields.forEach {
                                when {
                                    it.equals(fname) -> {
                                        assertNotEquals(action[it].asText(), uaction[it].asText())
                                        assertEquals(au[it].asText(), uaction[it].asText())
                                    }
                                    else -> assertEquals(action[it].asText(), uaction[it].asText())
                                }
                            }
                        }
                    }
                }

                assertEquals(200, DELETE("/action/$aid").code())
            }

            assertEquals(200, DELETE("/rule/$rid").code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testActionCreate"))
    fun testActionReorder() {
        with(createRule()) {
            val rid = path("id").asLong()
            var actions = arrayOf(
                    createRuleAction(rid),
                    createRuleAction(rid),
                    createRuleAction(rid)
            )

            assertTrue(actions[0]["ordinal"].asLong() < actions[1]["ordinal"].asLong())
            assertTrue(actions[1]["ordinal"].asLong() < actions[2]["ordinal"].asLong())


            with(GET("/rule/$rid/actions/select")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(3, size())
                    forEachIndexed { index, rule ->
                        assertEquals(actions[index]["id"].asLong(), rule["id"].asLong())
                    }
                }
            }

            // actually, nothing changed
            assertEquals(204, POST("/action/${actions[2]["id"].asLong()}/move/down").code())
            assertEquals(204, POST("/action/${actions[0]["id"].asLong()}/move/up").code())
            with(GET("/rule/$rid/actions/select")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(3, size())
                    forEachIndexed { index, rule ->
                        assertEquals(actions[index]["id"].asLong(), rule["id"].asLong())
                    }
                }
            }

            // swap first and second action
            assertEquals(204, POST("/action/${actions[0]["id"].asLong()}/move/down").code())
            actions = arrayOf(actions[1], actions[0], actions[2])
            with(GET("/rule/$rid/actions/select")) {
                assertEquals(200, code())
                with(mapper.readTree(body())) {
                    assertTrue(isArray)
                    assertEquals(3, size())
                    forEachIndexed { index, rule ->
                        assertEquals(actions[index]["id"].asLong(), rule["id"].asLong())
                    }
                }
            }

            actions.forEach {
                assertEquals(200, DELETE("/action/${it["id"].asLong()}").code())
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
                assertTrue(path("enabled").asBoolean())

                @Suppress("LABEL_NAME_CLASH")
                return this@with
            }
        }
    }

    private fun createRuleFilter(rid: Long): JsonNode {
        val filter = mapper.createObjectNode()
                .put("type", RandomStringUtils.randomAlphabetic(8).toLowerCase())
                .put("spec", RandomStringUtils.randomAscii(1024))
                .put("description", RandomStringUtils.randomAscii(32))
        with(PUT("/rule/$rid/filter").contentType("application/json").send(mapper.writeValueAsString(filter))) {
            assertEquals(200, code())
            with(mapper.readTree(body())) {
                assertTrue(isObject)
                assertTrue(hasNonNull("id"))
                assertTrue(hasNonNull("type"))
                assertEquals(filter["type"].asText(), path("type").asText(null))
                assertEquals(filter["spec"].asText(), path("spec").asText(null))
                assertEquals(filter["description"].asText(), path("description").asText(null))

                @Suppress("LABEL_NAME_CLASH")
                return this@with
            }
        }
    }

    private fun createRuleAction(rid: Long): JsonNode {
        val action = mapper.createObjectNode()
                .put("type", RandomStringUtils.randomAlphabetic(8).toLowerCase())
                .put("spec", RandomStringUtils.randomAscii(1024))
                .put("description", RandomStringUtils.randomAscii(32))
        with(PUT("/rule/$rid/action").contentType("application/json").send(mapper.writeValueAsString(action))) {
            assertEquals(200, code())
            with(mapper.readTree(body())) {
                assertTrue(isObject)
                assertTrue(hasNonNull("id"))
                assertTrue(hasNonNull("type"))
                assertTrue(hasNonNull("ordinal"))
                assertEquals(action["type"].asText(), path("type").asText(null))
                assertEquals(action["spec"].asText(), path("spec").asText(null))
                assertEquals(action["description"].asText(), path("description").asText(null))

                @Suppress("LABEL_NAME_CLASH")
                return this@with
            }
        }
    }
}
