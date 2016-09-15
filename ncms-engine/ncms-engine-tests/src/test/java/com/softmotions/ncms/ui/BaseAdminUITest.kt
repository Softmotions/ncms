package com.softmotions.ncms.ui

import org.oneandone.qxwebdriver.ui.Widget
import org.oneandone.qxwebdriver.ui.table.Table
import org.openqa.selenium.Keys
import org.testng.Assert

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class BaseAdminUITest(db: String) : BaseQXTest(db) {

    protected val assemblies = Assemblies()


    inner class Assemblies {


        ///////////////////////////////////////////////////////////
        //                      Assemblies                       //
        ///////////////////////////////////////////////////////////


        fun goTo() {
            findWidget("*/ncms.Toolbar/*/[@label=Assemblies]").click()
        }

        fun createAssembly(name: String) {
            val w = waitForWidget("*/ncms.asm.AsmNav");
            actions.moveToElement(w.findWidget("*/qx.ui.table.pane.Scroller").contentElement)
            actions.contextClick()
            actions.perform()

            // Create new assembly
            findWidget("*/qx.ui.menu.Menu/*/[@label=New assembly]").click()
            actions.sendKeys(name).perform()
            findWidget("*/ncms.asm.AsmNewDlg/*/[@label=Save]").click()
        }

        fun selectAssembly(name: String) {
            val table: Table = findWidget("*/ncms.asm.AsmTable") as Table
            var ind: Long = table.getRowIndexForCellText(1, name)
            if (ind < 0) {
                val w = findWidget("*/sm.ui.form.SearchField/*/qx.ui.form.TextField")
                w.executeInWidget("""
            this.setValue('');
            this.focus();
            """)
                actions.sendKeys(name).perform()
            }
            driverWait5.until {
                ind = table.getRowIndexForCellText(1, name)
                ind >= 0
            }
            table.selectRow(ind)
        }


        fun setBasicAssemblyParams(
                description: String? = null,
                templateMode: String? = null) {

            val w = findWidget("*/ncms.asm.AsmEditor/qx.ui.core.scroll.ScrollPane/sm.ui.form.FlexFormRenderer")
            w.executeInWidget("""
           var items = this._form.getItems();
           var d = items['description'];
           d.focus();
           ${if (description != null) "d.setValue('${description}');" else ""};
           d.blur();
           ${if (templateMode != null) "items['templateMode'].setModelSelection(['${templateMode}']);" else ""}
        """)


        }

        fun createBasicAttribute(type: String,
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
            ${if (label != null) "items['label'].setValue('${label}');" else ""}
            items['required'].setValue(${required});
        """)
        }


        fun removeAttribute(name: String) {
            findAttribute(name, select = true)
            findWidget("*/ncms.asm.AsmAttrsTable/*/[@icon=.*delete.png]").click()
            findWidget("*/sm.dialog.Confirm/*/[@label=Yes]").click()
            driverWait5.until {
                (findWidget("*/ncms.asm.AsmAttrsTable/sm.table.Table") as Table).getRowIndexForCellText(1, name) == -1L
            }
        }


        fun findAttribute(name: String,
                          select: Boolean = false,
                          invert: Boolean = false): Pair<Table, Long> {
            // Check attribute is saved
            val table = findWidget("*/ncms.asm.AsmAttrsTable/sm.table.Table") as Table
            val ind = table.getRowIndexForCellText(1, name)
            if (invert) {
                Assert.assertEquals(ind, -1L)
            } else {
                Assert.assertTrue(ind > -1L)
                if (select) {
                    table.selectRow(ind)
                }
            }
            return table.to(ind)
        }

        fun checkAttributeExists(name: String,
                                 type: String,
                                 label: String? = null,
                                 value: String? = null,
                                 select: Boolean = false) {

            val ret = findAttribute(name, select)
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

        fun attrDlgClickSave() {
            // Press the OK and save the assembly attribute
            val w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/[@label=Ok]")
            Assert.assertEquals(w.classname, "qx.ui.form.Button")
            w.click()
        }

        fun createStringAttr(name: String,
                             required: Boolean = false,
                             maxLength: Int = 0,
                             label: String? = null,
                             value: String? = null) {

            createBasicAttribute("string", name, label, required)

            val w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/sm.ui.cont.LazyStack/sm.ui.form.FlexFormRenderer")
            w.executeInWidget("""
            var items = this._form.getItems();
            items['maxLength'].setValue(${maxLength});
            items['value'].setValue('${value ?: ""}');
        """)
            attrDlgClickSave()
            checkAttributeExists(
                    name = name,
                    type = "string",
                    label = label,
                    value = value
            )
        }

        fun createAliasAttr(name: String = "alias",
                            required: Boolean = false,
                            label: String? = "Alias",
                            value: String? = null) {

            createBasicAttribute("alias", name, label, required)
            val w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/sm.ui.cont.LazyStack/sm.ui.form.FlexFormRenderer")
            w.executeInWidget("""
            var items = this._form.getItems();
            items['alias'].setValue('${value ?: ""}');
        """)
            attrDlgClickSave()
            checkAttributeExists(
                    name = name,
                    type = "alias",
                    label = label,
                    value = value
            )
        }

        fun addAssemblyParent(parentName: String) {

            // Add new assembly parent
            findWidget("*/ncms.asm.AsmParentsTable/*/[@icon=.*add.png]").click()
            var table = findWidget("*/ncms.asm.AsmSelectorDlg/*/ncms.asm.AsmTable") as Table
            actions.sendKeys(parentName).sendKeys(Keys.DOWN).perform()
            var ind = table.getRowIndexForCellText(1, parentName)
            Assert.assertTrue(ind > -1)
            table.selectRow(ind)
            findWidget("*/ncms.asm.AsmSelectorDlg/*/[@label=Ok]").click()

            table = findWidget("*/ncms.asm.AsmParentsTable/*/sm.table.Table") as Table
            ind = table.getRowIndexForCellText(0, parentName)
            Assert.assertTrue(ind > -1)
            table.selectRow(ind)
        }

    }


}