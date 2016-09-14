package com.softmotions.ncms.ui

import org.oneandone.qxwebdriver.By.qxh
import org.oneandone.qxwebdriver.ui.Widget
import org.oneandone.qxwebdriver.ui.table.Table
import org.openqa.selenium.Keys
import org.testng.Assert.*
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Test(groups = arrayOf("ui"))
class _TestSimpleSiteUI(db: String) : BaseQXTest(db) {

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
        findWidget("*/ncms.Toolbar/*/[@label=Assemblies]").click()

        val asmNav = waitForWidget("*/ncms.asm.AsmNav");
        actions.moveToElement(asmNav.findWidget("*/qx.ui.table.pane.Scroller").contentElement)
        actions.contextClick()
        actions.perform()

        // Create new assembly
        findWidget("*/qx.ui.menu.Menu/*/[@label=New assembly]").click()
        actions.sendKeys("base").perform()
        findWidget("*/ncms.asm.AsmNewDlg/*/[@label=Save]").click()

        var table: Table = findWidget("*/ncms.asm.AsmTable") as Table
        var ind: Long = table.getRowIndexForCellText(1, "base")
        assertTrue(ind > -1)
        table.selectRow(ind)

        // Add new assembly attribute
        findWidget("*/ncms.asm.AsmAttrsTable/*/[@icon=.*add.png]").click()
        var w: Widget = findWidget("*/ncms.asm.AsmAttributeTypeSelectorDlg")
        actions.sendKeys("stri").sendKeys(Keys.DOWN).perform()
        table = w.findWidget("*/sm.table.Table") as Table
        assertNotNull(table.selectedRanges)
        assertEquals(table.selectedRanges.size, 1)
        w.findWidget("*/[@label=Ok]").click()

        // Fill the assembly attributes
        w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/sm.ui.form.FlexFormRenderer")
        w.executeInWidget("""
            var items = this._form.getItems();
            items['name'].setValue('title');
            items['label'].setValue('Title');
            items['required'].setValue(true);
        """)
        w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/sm.ui.cont.LazyStack/sm.ui.form.FlexFormRenderer")
        w.executeInWidget("""
            var items = this._form.getItems();
            items['maxLength'].setValue(64)
            items['value'].setValue('The default title')
        """)

        // Press the OK and save the assembly attribute
        w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/[@label=Ok]")
        assertEquals(w.classname, "qx.ui.form.Button")
        w.click()


        // Check attribute is saved
        table = findWidget("*/ncms.asm.AsmAttrsTable/sm.table.Table") as Table
        ind = table.getRowIndexForCellText(1, "title")
        assertTrue(ind > -1)
        assertEquals(table.getCellText(ind, 2), "Title")
        assertEquals(table.getCellText(ind, 3), "string")
        assertEquals(table.getCellText(ind, 4), "The default title")

        // Set assembly description
        w = findWidget("*/ncms.asm.AsmEditor/qx.ui.core.scroll.ScrollPane/sm.ui.form.FlexFormRenderer")
        w.executeInWidget("""
           var items = this._form.getItems();
           var d = items['description'];
           d.focus();
           d.setValue('Base assembly');
           d.blur();
        """)
    }
}