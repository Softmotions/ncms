package com.softmotions.ncms.ui

import org.oneandone.qxwebdriver.By.qxh
import org.openqa.selenium.chrome.ChromeOptions
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Test(groups = arrayOf("ui"))
class _TestSimpleSiteUI(db: String) : BaseAdminUITest(db) {

    constructor() : this(DEFAULT_DB) {
    }

    @BeforeClass
    override fun setup() {
        super.setup()
        driver.get(R("/adm/index.html"))
    }

    @AfterClass
    override fun shutdown() = super.shutdown()


    override fun initDriver(driverType: String, options: ChromeOptions) {
        //options.addArguments("start-maximized")
        options.addArguments("log-level=0", "enable-logging=stderr")
        super.initDriver(driverType, options)
    }

    @Test
    fun testToolbarLabels() {
        waitForPresence(qxh("*/ncms.Toolbar/*/[@label=Pages]"))
        findWidget("*/ncms.Toolbar/*/[@label=News]")
        findWidget("*/ncms.Toolbar/*/[@label=Media]")
        findWidget("*/ncms.Toolbar/*/[@label=Assemblies]")
        findWidget("*/ncms.Toolbar/*/[@label=Tools]")
    }

    @Test
    fun testCreateBasicAssemblies() {

        val a = assemblies.activate()

        a.createAssembly("basic")
        a.selectAssembly("basic");
        a.setBasicAssemblyParams(description = "Basic assembly")

        a.createStringAttr(
                name = "title",
                label = "Title",
                required = true,
                maxLength = 64,
                value = "4ca24da751ac4899a56a13a4091a0f6f"
        );
        a.createStringAttr(
                name = "extra",
                value = "3d70f55efd8e4e5b9cff1479103be115"
        );
        a.createStringAttr(
                name = "extra2"
        );
        a.createAliasAttr()


        a.createAssembly("basic_content")
        a.selectAssembly("basic_content")
        a.setBasicAssemblyParams(
                description = "Simple page with content",
                templateMode = "page"
        )
        a.addAssemblyParent("basic")
        a.checkAttributeExists(
                name = "extra2",
                type = "string"
        )
        a.checkAttributeExists(
                name = "extra",
                type = "string",
                value = "3d70f55efd8e4e5b9cff1479103be115",
                select = true
        )
        // Add optional mediawiki attribute
        a.createWikiAttr("content", "Content", required = true)


        // Test removal of attribute
        a.selectAssembly("basic")
        a.removeAttribute("extra2")
        a.checkAttributeExists(name = "extra2", invert = true)


        // In basic_content set core
        a.selectAssembly("basic_content")
        a.openSelectCoreDlg()

        val f = selectFileDlg
        f.newFile("basic_content_core.httl")
        f.newFile("to_be_deleted.txt")
        f.deleteFile("to_be_deleted.txt")

        f.setFileTextualContent("basic_content_core.httl", """
            <html>
               <head>
                 <title>${D}{asm('title')}</title>
               </head>
               <h1>Simple page</h1>
               <p>Extra=${D}{'extra'.asm}</p>

               ${D}{'content'.asm}

            </html>
        """)
        f.ok()

        driverWait5.until {
            a.getAsmCoreValue() == "/basic_content_core.httl"
        }
    }


    @Test(dependsOnMethods = arrayOf("testCreateBasicAssemblies"))
    fun testCreatePageOnBasicContentAssembly() {

        val p = pages.activate()
        val sitedir = p.newPage(
                context = p.selectPageNode(label = "Pages"),
                name = "TestSimpleSiteUI",
                isDirectory = true)

        val site1 = p.newPage(
                context = sitedir,
                name = "Site1",
                isDirectory = false)

        val editPane = p.activatePageEdit()
        editPane.setPageTemplate("basic_content")


        //waitForever()
    }

}