package com.softmotions.ncms.rs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kevinsawicki.http.HttpRequest
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.net.BCodec
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
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
                    assertEquals(200, DELETE("/delete/${it.path("folder").asText()}${it.path("name").asText()}").code())
                }
            }
        }

        with(GET("/select/count")) {
            assertEquals(200, code())
            assertEquals("0", body())
        }
    }

    @Test(dependsOnMethods = arrayOf("testMediaFilePut", "testMediaFileDelete", "testMediaFolderPut", "testMediaFolderDelete"))
    fun testMediaFileGet() {
        val testFolder = putFolder().path("label").asText()
        for (fileType in listOf("txt", "svg", "png")) { // test: different file types
            for (folderName in listOf("", testFolder)) { // test: / and subfolder
                with(putFile(folderName, fileType)) {
                    val fileName = path("name").asText()
                    val folder = prepareFolder(path("folder").asText())
                    val fileSize = path("size").asLong()
                    val fileContentType = path("contentType").asText()
                    val reqIs = Files.newInputStream(getTestFile(fileType).toPath())
                    val reqMd5 = DigestUtils.md5Hex(IOUtils.toByteArray(reqIs))

                    for (j in 0..1) { // get by: 0 - name, 1 - id
                        var resource = ""
                        if (j == 0) {
                            resource = "/file/$folder$fileName"
                        } else {
                            with(GET("/select?folder=/$folder")) {
                                assertEquals(200, code())
                                val body = body()
                                assertNotNull(body)
                                with(mapper.readTree(body)) {
                                    assertTrue(isArray)
                                    forEach {
                                        assertTrue(it.hasNonNull("id"))
                                        if (fileName == it.path("name").asText(null)) {
                                            resource = "/fileid/" + it.path("id").asLong()
                                        }
                                    }
                                }
                            }
                        }

                        for (i in 0..1) { // request: 0 - HEAD, 1 - GET
                            val req: HttpRequest
                            if (i == 0) {
                                req = HEAD(resource)
                            } else {
                                req = GET(resource)
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
                                    val tempFile = File("/tmp/$fileName")
                                    receive(tempFile)
                                    assertEquals(fileSize, tempFile.length())
                                    val resIs = Files.newInputStream(tempFile.toPath())
                                    val resMd5 = DigestUtils.md5Hex(IOUtils.toByteArray(resIs))
                                    assertEquals(resMd5, reqMd5)
                                    tempFile.delete()
                                }
                            }
                        }
                    }
                    delete(folder, fileName)
                }
            }
        }
        delete("", testFolder)
    }

    @Test(dependsOnMethods = arrayOf("testMediaFileGet"))
    fun testMediaFileThumb() {
        val testFolder = putFolder().path("label").asText()
        for (folderName in listOf("", testFolder)) { // test: / and subfolder
            with(putFile(folderName, "png")) {
                val fileName = path("name").asText()
                val folder = prepareFolder(path("folder").asText())
                val fileContentType = path("contentType").asText()

                for (j in 0..1) { // get by: 0 - name, 1 - id
                    var resource = ""
                    if (j == 0) {
                        resource = "/thumbnail/$folder$fileName"
                    } else {
                        with(GET("/select?folder=/$folder")) {
                            assertEquals(200, code())
                            val body = body()
                            assertNotNull(body)
                            with(mapper.readTree(body)) {
                                assertTrue(isArray)
                                forEach {
                                    assertTrue(it.hasNonNull("id"))
                                    if (fileName == it.path("name").asText(null)) {
                                        resource = "/thumbnail2/" + it.path("id").asLong()
                                    }
                                }
                            }
                        }
                    }

                    with(GET(resource)) {
                        assertEquals(200, code())
                        val headers = headers()
                        val respCType = headers["Content-Type"]?.get(0)
                        assertEquals(fileContentType, respCType)
                    }
                }
                delete(folder, fileName)
            }
        }
        delete("", testFolder)
    }

    @Test(dependsOnMethods = arrayOf("testMediaSelect"))
    fun testMediaFolderSelect() {
        with(GET("/folders")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(0, size())
            }
        }
    }

    @Test(dependsOnMethods = arrayOf("testMediaFolderSelect"))
    fun testMediaFolderPut() {
        val folderName = RandomStringUtils.randomAlphanumeric(11)
        with(PUT("/folder/$folderName")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertEquals(folderName, path("label").asText(null))
                assertEquals(1, path("status").asInt())
                assertTrue(hasNonNull("system"))
            }
        }
        with(PUT("/folder/$folderName")) {
            assertEquals(500, code())
        }
    }

    @Test(dependsOnMethods = arrayOf("testMediaFolderPut"))
    fun testMediaFolderDelete() {
        with(GET("/folders")) {
            assertEquals(200, code())

            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                forEach {
                    assertTrue(it.hasNonNull("label"))
                    assertEquals(1, it.path("status").asInt())
                    assertEquals(200, DELETE("/delete/${it.path("label").asText()}").code())
                }
            }
        }

        with(GET("/folders")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertTrue(isArray)
                assertEquals(0, size())
            }
        }
    }

    @Test(dependsOnMethods = arrayOf("testMediaFilePut", "testMediaFileDelete", "testMediaFolderPut", "testMediaFolderDelete"))
    fun testMediaListing() {
        /*
        * /
        * |-file1
        * \-folder1
        *   |---file2
        *   \---folder2
        *       \---file3
        */
        testMediaSelect()
        val file1 = putFile().path("name").asText()
        val folder1 = putFolder().path("label").asText()
        val file2 = putFile(folder1).path("name").asText()
        val folder2 = putFolder(folder1).path("label").asText()
        val file3 = putFile(folder1 + "/" + folder2).path("name").asText()

        for (i in 0..2) {
            val folder: String
            val folderName: String
            val fileName: String
            if (i == 0) {
                folder = ""
                folderName = folder1
                fileName = file1
            } else if (i == 1) {
                folder = "/$folder1"
                folderName = folder2
                fileName = file2
            } else if (i == 2) {
                folder = "/$folder1/$folder2"
                folderName = "" // doesn't matter - folder not contain folders
                fileName = file3
            } else {
                folder = ""
                folderName = ""
                fileName = ""
            }

            // test files listing
            with(GET("/files$folder")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    forEach {
                        assertEquals(fileName, it.path("label").asText(null))
                        assertEquals(0, it.path("status").asInt())
                    }
                }
            }

            // test folder listing
            with(GET("/folders$folder")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    forEach {
                        assertEquals(folderName, it.path("label").asText(null))
                        assertEquals(1, it.path("status").asInt())
                    }
                }
            }

            // test "all" listing
            with(GET("/all$folder")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    forEach {
                        val status = it.path("status").asInt()
                        val fileLabel = if (status == 0) {
                            fileName
                        } else {
                            folderName
                        }
                        assertEquals(fileLabel, it.path("label").asText(null))
                    }
                }
            }
        }

        delete(folder2, file3)
        delete(folder1, folder2)
        delete(folder1, file2)
        delete("", folder1)
        delete("", file1)
    }

    @Test(dependsOnMethods = arrayOf("testMediaFilePut", "testMediaFileDelete", "testMediaFolderPut", "testMediaFolderDelete"))
    fun testMediaMove() {
        val testFolder1 = putFolder().path("label").asText()
        val testFolder2 = putFolder().path("label").asText()
        with(putFile()) {
            val fileName = path("name").asText()
            with(PUT("/move/$fileName").contentType("application/json").send("$testFolder1/$fileName")) {
                assertEquals(204, code())
            }

            with(GET("/select?folder=/$testFolder1")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    forEach {
                        assertTrue(it.hasNonNull("id"))
                        assertEquals(fileName, it.path("name").asText(null))
                    }
                }
            }

            with(PUT("/move/$testFolder1").contentType("application/json").send("$testFolder2/$testFolder1")) {
                assertEquals(204, code())
            }
            with(GET("/select?folder=/$testFolder2/$testFolder1")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    forEach {
                        assertTrue(it.hasNonNull("id"))
                        assertEquals(fileName, it.path("name").asText(null))
                    }
                }
            }

            delete(testFolder2 + "/" + testFolder1, fileName)
        }
        delete(testFolder2, testFolder1)
        delete("", testFolder2)
    }

    @Test(dependsOnMethods = arrayOf("testMediaFilePut", "testMediaFileDelete", "testMediaFolderPut", "testMediaFolderDelete"))
    fun testMediaCopyDeleteBatch() {
        val testFolder = putFolder().path("label").asText()
        with(putFile()) {
            val fileName = path("name").asText()
            var props = mapper.createArrayNode().add(fileName)

            with(PUT("/copy-batch/$testFolder").contentType("application/json").send(mapper.writeValueAsString(props))) {
                assertEquals(204, code())
            }

            with(GET("/select?folder=/$testFolder")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    forEach {
                        assertTrue(it.hasNonNull("id"))
                        assertEquals(fileName, it.path("name").asText(null))
                    }
                }
            }

            props = mapper.createArrayNode().
                    add("$testFolder/$fileName").
                    add(fileName).
                    add(testFolder)
            with(DELETE("/delete-batch").contentType("application/json").send(mapper.writeValueAsString(props))) {
                assertEquals(204, code())
            }
            with(GET("/select/count")) {
                assertEquals(200, code())
                assertEquals("0", body())
            }
        }
    }

    @Test(dependsOnMethods = arrayOf("testMediaFilePut", "testMediaFileDelete", "testMediaFolderPut", "testMediaFolderDelete"))
    fun testMediaMeta() {
        val testFolder = putFolder().path("label").asText()
        with(putFile(testFolder)) {
            val fileName = path("name").asText()

            var fileId = 0L
            with(GET("/select?folder=/$testFolder")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertTrue(isArray)
                    assertEquals(1, size())
                    forEach {
                        assertTrue(it.hasNonNull("id"))
                        assertEquals(fileName, it.path("name").asText(null))
                        fileId = it.path("id").asLong()
                    }
                }
            }

            with(GET("/path/$fileId")) {
                assertEquals(200, code())
                assertEquals("/$testFolder/$fileName", body())
            }

            with(GET("/meta/$fileId")) {
                assertEquals(200, code())
                val body = body()
                assertNotNull(body)
                with(mapper.readTree(body)) {
                    assertEquals(fileId, path("id").asLong())
                    assertEquals("/$testFolder/", path("folder").asText(null))
                    assertEquals(fileName, path("name").asText(null))
                }
            }

            with(POST("/meta/$fileId").contentType("application/x-www-form-urlencoded").form("description", "test")) {
                assertEquals(204, code())
            }

            delete(testFolder, fileName)
        }
        delete("", testFolder)
    }

    private fun putFile(folder: String = "", type: String = "txt"): JsonNode {
        val fileExt: String
        val contentType: String
        if ("txt" == type) {
            contentType = "text/plain"
            fileExt = "txt"
        } else if ("png" == type) {
            contentType = "image/png"
            fileExt = "png"
        } else if ("svg" == type) {
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

    private fun putFolder(folder: String = ""): JsonNode {
        val folderLabel = RandomStringUtils.randomAlphanumeric(11)
        val folderName = prepareFolder(folder)

        with(PUT("/folder/$folderName$folderLabel")) {
            assertEquals(200, code())
            val body = body()
            assertNotNull(body)
            with(mapper.readTree(body)) {
                assertEquals(folderLabel, path("label").asText(null))
                assertEquals(1, path("status").asInt())
                assertTrue(hasNonNull("system"))

                @Suppress("LABEL_NAME_CLASH")
                return this@with
            }
        }
    }

    private fun delete(folder: String = "", fileName: String) {
        val code = DELETE("/delete/${prepareFolder(folder)}$fileName").code()
        assertTrue(code == 200 || code == 204)
    }

    private fun prepareFolder(folder: String): String {
        if ("" != folder && !folder.endsWith("/")) {
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