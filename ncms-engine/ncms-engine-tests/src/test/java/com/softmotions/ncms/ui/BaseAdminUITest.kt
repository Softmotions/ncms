package com.softmotions.ncms.ui

import org.oneandone.qxwebdriver.ui.Widget
import org.oneandone.qxwebdriver.ui.table.Table
import org.openqa.selenium.Keys
import org.testng.Assert

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class BaseAdminUITest(db: String) : BaseQXTest(db) {


    protected fun goToAssemblies() {
        findWidget("*/ncms.Toolbar/*/[@label=Assemblies]").click()
    }

    protected fun inAssembliesCreateAssembly(name: String) {
        val w = waitForWidget("*/ncms.asm.AsmNav");
        actions.moveToElement(w.findWidget("*/qx.ui.table.pane.Scroller").contentElement)
        actions.contextClick()
        actions.perform()

        // Create new assembly
        findWidget("*/qx.ui.menu.Menu/*/[@label=New assembly]").click()
        actions.sendKeys(name).perform()
        findWidget("*/ncms.asm.AsmNewDlg/*/[@label=Save]").click()
    }

    protected fun inAssembliesSelectAssembly(name: String,
                                             description: String? = null) {

        val table: Table = findWidget("*/ncms.asm.AsmTable") as Table
        val ind: Long = table.getRowIndexForCellText(1, name)
        Assert.assertTrue(ind > -1)
        table.selectRow(ind)

        // Set assembly description
        if (description != null) {
            val w = findWidget("*/ncms.asm.AsmEditor/qx.ui.core.scroll.ScrollPane/sm.ui.form.FlexFormRenderer")
            w.executeInWidget("""
           var items = this._form.getItems();
           var d = items['description'];
           d.focus();
           d.setValue('${description}');
           d.blur();
        """)
        }
    }

    protected fun inAssembliesCreateBasicAttribute(type: String,
                                                   name: String,
                                                   label: String? = null,
                                                   required: Boolean = false) {
        // Add new assembly attribute
        findWidget("*/ncms.asm.AsmAttrsTable/*/[@icon=.*add.png]").click()
        var w: Widget = findWidget("*/ncms.asm.AsmAttributeTypeSelectorDlg")
        actions.sendKeys(type).sendKeys(Keys.DOWN).perform()
        val table = w.findWidget("*/sm.table.Table") as Table
        Assert.assertNotNull(table.selectedRanges)
        Assert.assertEquals(table.selectedRanges.size, 1)
        w.findWidget("*/[@label=Ok]").click()

        // Fill the assembly attributes
        w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/sm.ui.form.FlexFormRenderer")
        w.executeInWidget("""
            var items = this._form.getItems();
            items['name'].setValue('${name}');
            ${if (label != null) {
            "items['label'].setValue('${label}');"
        } else {
            ""
        }}
            items['required'].setValue(${required});
        """)
    }

    protected fun inAssembliesSelectAttribute(name: String): Pair<Table, Long> {
        // Check attribute is saved
        val table = findWidget("*/ncms.asm.AsmAttrsTable/sm.table.Table") as Table
        val ind = table.getRowIndexForCellText(1, name)
        Assert.assertTrue(ind > -1)
        return table.to(ind)
    }

    protected fun inAssembliesCheckAttributeTable(name: String,
                                                  type: String,
                                                  label: String? = null,
                                                  value: String? = null) {

        val ret = inAssembliesSelectAttribute(name)
        val table = ret.first
        val ind = ret.second
        Assert.assertEquals(table.getCellText(ind, 3), type)
        if (label != null) {
            Assert.assertEquals(table.getCellText(ind, 2), label)
        }
        if (value != null) {
            Assert.assertEquals(table.getCellText(ind, 4), value)
        }
    }

    protected fun inAssembliesCreateStringAttr(name: String,
                                               required: Boolean = false,
                                               maxLength: Int = 0,
                                               label: String? = null,
                                               value: String? = null) {

        inAssembliesCreateBasicAttribute("string", name, label, required)

        var w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/sm.ui.cont.LazyStack/sm.ui.form.FlexFormRenderer")
        w.executeInWidget("""
            var items = this._form.getItems();
            items['maxLength'].setValue(${maxLength});
            items['value'].setValue('${value ?: ""}');
        """)

        // Press the OK and save the assembly attribute
        w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/[@label=Ok]")
        Assert.assertEquals(w.classname, "qx.ui.form.Button")
        w.click()

        inAssembliesCheckAttributeTable(
                name = name,
                type = "string",
                label = label,
                value = value
        )
    }
}