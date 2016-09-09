package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kevinsawicki.http.HttpRequest
import org.apache.commons.codec.net.BCodec
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
    fun testMediaFilePut() {
        val fileName = RandomStringUtils.randomAlphanumeric(8) + "." + RandomStringUtils.randomAlphabetic(3)
        val file = getTestFile()

        with(PUT("/file/$fileName").contentType("text/plain").send(file)) {
            assertEquals(204, code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testMediaFilePut"))
    fun testMediaFileDelete() {
        with(GET("/select")) {
            assertEquals(200, code())

            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                forEach {
                    assertTrue(it.hasNonNull("id"))
                    assertTrue(it.hasNonNull("folder"))
                    assertTrue(it.hasNonNull("name"))
                    assertEquals(204, DELETE("/delete/${it.path("folder").asText()}${it.path("name").asText()}").code())
                }
            }
        }

        with(GET("/select/count")) {
            assertEquals(200, code())
            assertEquals("0", body())
        }
    }

    @Test(dependsOnMethods = arrayOf("testMediaFilePut", "testMediaFileDelete"))
    fun testMediaFileGet() {
        // todo: on png (or pdf) response body length less than file size!
        for (fileType in listOf("txt", "svg")) {
            with(putFile("", fileType)) {
                val fileName = path("name").asText()
                val folder = prepareFolder(path("folder").asText())
                val fileSize = path("size").asInt()
                val fileContentType = path("contentType").asText()
                for (i in 0..1) {
                    val req: HttpRequest
                    if (i == 0) {
                        req = HEAD("/file/$folder$fileName")
                    } else {
                        req = GET("/file/$folder$fileName")
                    }
                    with(req) {
                        if (i == 0) {
                            assertEquals(204, code())
                        } else {
                            assertEquals(200, code())
                        }

                        val headers = headers()

                        val respFName = BCodec("UTF-8").decode(
                                headers["Content-Disposition"]?.get(0)?.
                                        removePrefix("attachment; filename=\"")?.
                                        removeSuffix("\""))
                        assertEquals(fileName, respFName)

                        val respCLeng = if (i == 0) {
                            headers["X-Content-Length"]?.get(0)
                        } else {
                            headers["Content-Length"]?.get(0)
                        }
                        assertEquals(fileSize.toString(), respCLeng)

                        val respEnc = headers["Content-Encoding"]?.get(0)
                        val respCType = headers["Content-Type"]?.get(0)
                        if (respEnc != null) {
                            assertEquals(fileContentType + ";charset=" + respEnc, respCType)
                        } else {
                            assertEquals(fileContentType, respCType)
                        }

                        if (i == 1) {
                            val body = body()

                            log.info("headers: {}", headers)
                            log.info("body.len: {}", body.length)
                            assertEquals(fileSize, body.length)
                        }
                    }
                }

                deleteFile(folder, fileName)
            }
        }
    }

    private fun putFile(folder: String = "", type: String = "txt"): JsonNode {
        val fileExt: String
        val contentType: String
        if ("txt".equals(type)) {
            contentType = "text/plain"
            fileExt = "txt"
        } else if ("png".equals(type)) {
            contentType = "image/png"
            fileExt = "png"
        } else if ("svg".equals(type)) {
            contentType = "image/svg+xml"
            fileExt = "svg"
        } else {
            contentType = "text/plain"
            fileExt = "txt"
        }

        val fileName = RandomStringUtils.randomAlphanumeric(8) + "." + fileExt
        val file = getTestFile(type)
        val folderName = prepareFolder(folder)

        with(PUT("/file/$folderName$fileName").contentType(contentType).send(file)) {
            assertEquals(204, code())
        }

        return mapper.createObjectNode().
                put("name", fileName).
                put("folder", folderName).
                put("size", file.length()).
                put("contentType", contentType)
    }

    private fun deleteFile(folder: String = "", fileName: String) {
        assertEquals(204, DELETE("/delete/${prepareFolder(folder)}$fileName").code())
    }

    private fun prepareFolder(folder: String): String {
        if (!"".equals(folder) && !folder.endsWith("/")) {
            return folder + "/"
        } else {
            return folder
        }
    }

    private fun getTestFile(type: String = "txt"): File {
        val fileName = when (type) {
            "txt" -> "file.txt"
            "png" -> "file.png"
            "svg" -> "file.svg"
            else -> "file.txt"
        }
        return File(Paths.get(System.getProperty("project.basedir"), "/src/test/java/com/softmotions/ncms/rs/data/$fileName").toString())
    }
}