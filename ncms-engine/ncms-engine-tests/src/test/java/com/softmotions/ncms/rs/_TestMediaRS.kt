package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("rs"))
class _TestMediaRS(db: String) : BaseRSTest(db) {

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

    override fun R(resource: String): String = super.R(resource = "/rs/media$resource")

    @Test()
    fun testMediaSelect() {
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

    @Test(dependsOnMethods = arrayOf("testMediaSelect"))
    fun testMediaPut() {
        val fileName = RandomStringUtils.randomAlphanumeric(8) + "." + RandomStringUtils.randomAlphabetic(3)
        val file = getTestFile()

        with(PUT("/file/$fileName").part("file", fileName, "image/png", file)) {
            assertEquals(204, code())
        }
    }

    private fun putFile() {
        val fileName = RandomStringUtils.randomAlphanumeric(8) + "." + RandomStringUtils.randomAlphabetic(3)
        val file = getTestFile()

        with(PUT("/file/$fileName").part("file", fileName, "image/png", file)) {
            assertEquals(204, code())
        }
    }

    private fun getTestFile(): File {
        return File(Paths.get(System.getProperty("project.basedir"), "/src/test/java/com/softmotions/ncms/rs/data/file.png").toString())
    }
}