package com.softmotions.ncms.ui

import org.oneandone.qxwebdriver.By.qxh
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
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

    @Test
    fun testToolbarLabels() {
        waitForPresence(qxh("*/ncms.Toolbar/*/[@label=Pages]"))
        findWidget("*/ncms.Toolbar/*/[@label=News]")
        findWidget("*/ncms.Toolbar/*/[@label=Media]")
        findWidget("*/ncms.Toolbar/*/[@label=Assemblies]")
        findWidget("*/ncms.Toolbar/*/[@label=Tools]")
    }

    @Test
    fun testCreateBaseAssembly() {
        goToAssemblies()
        inAssembliesCreateAssembly("basic")
        inAssembliesSelectAssembly("basic", "Basic assembly");

        inAssembliesCreateStringAttr(
                name = "title",
                label = "Title",
                required = true,
                maxLength = 64,
                value = "4ca24da751ac4899a56a13a4091a0f6f"
        );

        inAssembliesCreateStringAttr(
                name = "extra",
                value = "3d70f55efd8e4e5b9cff1479103be115"
        );


        waitForever()
    }
}