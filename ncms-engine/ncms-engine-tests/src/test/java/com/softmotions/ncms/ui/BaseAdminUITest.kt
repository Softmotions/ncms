package com.softmotions.ncms.ui

import org.apache.commons.lang3.StringEscapeUtils
import org.oneandone.qxwebdriver.By
import org.oneandone.qxwebdriver.ui.Widget
import org.oneandone.qxwebdriver.ui.form.MenuButton
import org.oneandone.qxwebdriver.ui.table.Table
import org.openqa.selenium.Keys
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.support.ui.ExpectedConditions
import org.testng.Assert.*
import kotlin.test.assertEquals

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class BaseAdminUITest(db: String) : BaseQXTest(db) {

    protected val assemblies = Assemblies()

    protected val selectFileDlg = SelectFileDlg()

    protected val pages = Pages()


    fun checkPopupNotificationShown(msg: String? = null) {
        //sm-app-popup//
        val w = qxd.findWidget(By.xpath("//div[contains(@class, 'sm-app-popup')]"))
        driverWait5.until(ExpectedConditions.visibilityOf(w))
        if (msg != null) {
            w.findWidget("*/[@label=${msg}]")
        }
    }

    inner class Pages {

        ///////////////////////////////////////////////////////////
        //                     In `Pages`                        //
        ///////////////////////////////////////////////////////////

        fun activate(): Pages {
            findWidget("*/ncms.Toolbar/*/[@label=Pages]").click()
            return this
        }

        fun selectPageNode(label: String): Widget {
            var w = findWidget("*/ncms.pgs.PagesTreeSelector/*/sm.ui.tree.ExtendedVirtualTree/*/[@label=${label}]")
            if (w.classname != "sm.ui.tree.ExtendedVirtualTreeItem") {
                w = w.findWidget(By.xpath("./ancestor-or-self::node()[@qxclass='sm.ui.tree.ExtendedVirtualTreeItem']"))
            }
            w.click()
            try {
                w.contentElement.findElement(By.qxh("*/[@source=.*plus.gif]"))?.click()
            } catch (ignored: NoSuchElementException) {
            }
            return w
        }

        /**
         * Create a new page over selected node in page tree
         */
        fun newPage(context: Widget,
                    name: String,
                    isDirectory: Boolean = false): Widget {
            actions.moveToElement(context.contentElement)
            actions.contextClick()
            actions.perform()
            findWidget("*/qx.ui.menu.Menu/*/[@label=New]").click()

            val dlg = findWidget("*/ncms.pgs.PageNewDlg")
            driverWait5.until {
                dlg.isDisplayed
            }
            actions.sendKeys(name).perform()
            dlg.findWidget("*/qx.ui.form.renderer.Single").executeInWidget("""
                    var items = this._form.getItems();
                    items['container'].setValue(${isDirectory});
                """)
            dlg.findWidget("*/[@label=Save]").click()
            return selectPageNode(name)
        }

        fun activatePageEdit(): PagesEdit {
            findWidget(By.xpath(
                    "(//${qxpn("ncms.pgs.PageEditor")}//${qxpn("qx.ui.tabview.TabButton")}//${qxpn("qx.ui.basic.Label", "Edit")})[1]"))
                    .click()
            return PagesEdit()
        }
    }

    inner class PagesEdit {

        fun setPageTemplate(template: String) {
            try {
                val el = qxd.findElement(By.qxh("*/ncms.pgs.PagesSelectorDlg"))
                if (el != null && el.isDisplayed) {
                    findWidget("*/ncms.pgs.PagesSelectorDlg/*/[@label=Cancel]").click()
                }
            } catch (ignored: NoSuchElementException) {
            }
            findWidget("*/ncms.pgs.PageEditorEditPage/*/sm.ui.form.ButtonField/*/[@label=Template]").click()
            actions.sendKeys(template).perform()

            // ncms.asm.AsmTable
            val table = findWidget("*/ncms.pgs.PagesSelectorDlg/*/ncms.asm.AsmTable") as Table
            var ind = -1L;
            driverWait5.until {
                ind = table.getRowIndexForCellText(1, template)
                ind >= 0
            }
            table.selectRow(ind)
            findWidget("*/ncms.pgs.PagesSelectorDlg/*/[@label=Ok]").click()
        }
    }

    inner class SelectFileDlg {

        ///////////////////////////////////////////////////////////
        //                  Select file dialog                   //
        ///////////////////////////////////////////////////////////

        fun waitForDialogVisible(): Widget {
            val w = findWidget("*/ncms.mmgr.MediaSelectFileDlg")
            driverWait5.until(ExpectedConditions.visibilityOf(w))
            return w
        }

        fun newFile(fileName: String) {
            var w = waitForDialogVisible()
            w = w.findWidget("*/[@icon=.*add.png]")
            w.click()
            w as MenuButton
            w.getSelectableItem("New file").click()
            actions.sendKeys(fileName).perform()
            findWidget("*/ncms.mmgr.MediaFileNewDlg/*/[@label=Save]").click()
            selectFile(fileName)
        }

        fun selectFile(fileName: String, invert: Boolean = false) {
            val w = findWidget("*/ncms.mmgr.MediaSelectFileDlg/*/ncms.mmgr.MediaFilesTable")
            w as Table
            var ind = -1L;
            driverWait5.until {
                ind = w.getRowIndexForCellText(0, fileName)
                if (invert) ind < 0 else ind > -1
            }
            if (!invert) {
                w.selectRow(ind)
            }
        }

        fun deleteFile(fileName: String) {
            var w = waitForDialogVisible()
            selectFile(fileName)
            w = w.findWidget("*/[@icon=.*delete.png]")
            w.click()
            w = findWidget("*/sm.dialog.Confirm/*/[@label=Yes]")
            w.click()
            selectFile(fileName, invert = true)
        }

        fun setFileTextualContent(fileName: String, content: String) {
            var w = waitForDialogVisible()
            selectFile(fileName)
            w = w.findWidget("*/[@icon=.*edit-document.png]")
            assertEquals("true", w.getPropertyValue("enabled")?.toString());
            w.click()
            w = findWidget("*/ncms.mmgr.MediaTextFileEditorDlg/*/ncms.mmgr.MediaTextFileEditor")
            w.findWidget(By.xpath("//textarea[contains(@class, 'ace_text-input')]"))
            w.executeInWidget("""this.setCode("${StringEscapeUtils.escapeEcmaScript(content.trimIndent())}");""")
            actions.sendKeys(Keys.chord(Keys.CONTROL, "s")).perform()
            checkPopupNotificationShown("File successfully saved")
            findWidget("*/ncms.mmgr.MediaTextFileEditorDlg/*/[@icon=.*close.gif]").click()
        }

        fun ok() {
            val w = findWidget("*/ncms.mmgr.MediaSelectFileDlg/*/[@label=Ok]")
            assertEquals("true", w.getPropertyValue("enabled")?.toString());
            w.click()
        }
    }

    inner class Assemblies {

        ///////////////////////////////////////////////////////////
        //                    In `Assemblies`                    //
        ///////////////////////////////////////////////////////////


        fun activate(): Assemblies {
            findWidget("*/ncms.Toolbar/*/[@label=Assemblies]").click()
            return this
        }

        fun openSelectCoreDlg(): Widget {
            findWidget("*/ncms.asm.AsmEditor/*/[@icon=.*core_link.png]").click();
            return selectFileDlg.waitForDialogVisible()
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
            assertNotNull(table.selectedRanges)
            assertEquals(table.selectedRanges.size, 1)
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
            var ind = -1L
            if (invert) {
                driverWait5.until {
                    ind = table.getRowIndexForCellText(1, name)
                    ind == -1L
                }
            } else {
                driverWait5.until {
                    ind = table.getRowIndexForCellText(1, name)
                    ind > -1L
                }
                if (select) {
                    table.selectRow(ind)
                }
            }
            return table.to(ind)
        }

        fun checkAttributeExists(name: String,
                                 type: String? = null,
                                 label: String? = null,
                                 value: String? = null,
                                 select: Boolean = false,
                                 invert: Boolean = false) {

            val ret = findAttribute(name, select, invert = invert)
            if (invert) {
                assertEquals(-1L, ret.second)
                return
            }
            val table = ret.first
            val ind = ret.second
            if (type != null) {
                assertEquals(table.getCellText(ind, 3), type)
            }
            if (label != null) {
                assertEquals(table.getCellText(ind, 2), label)
            }
            if (value != null) {
                assertEquals(table.getCellText(ind, 4), value)
            }
        }

        fun getAsmCoreValue(): String? {
            val w = findWidget("*/ncms.asm.AsmEditor/qx.ui.core.scroll.ScrollPane/sm.ui.form.FlexFormRenderer")
            return w.executeInWidget("""
                var items = this._form.getItems();
                return items['core'].getValue();
            """)?.toString()
        }

        fun attrDlgClickSave() {
            // Press the OK and save the assembly attribute
            val w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/[@label=Ok]")
            assertEquals(w.classname, "qx.ui.form.Button")
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

        fun createWikiAttr(name: String,
                           label: String,
                           required: Boolean = false,
                           type: String = "wiki",
                           value: String? = null) {

            createBasicAttribute("wiki", name, label, required)
            val w = findWidget("*/ncms.asm.AsmAttrEditorDlg/*/sm.ui.cont.LazyStack/sm.ui.form.FlexFormRenderer")
            /*w.executeInWidget("""
            var items = this._form.getItems();
            items['alias'].setValue('${value ?: ""}');
        """)*/
            attrDlgClickSave()
            checkAttributeExists(
                    name = name,
                    type = "wiki",
                    label = label,
                    value = value)
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
            assertTrue(ind > -1)
            table.selectRow(ind)
            findWidget("*/ncms.asm.AsmSelectorDlg/*/[@label=Ok]").click()

            table = findWidget("*/ncms.asm.AsmParentsTable/*/sm.table.Table") as Table
            ind = table.getRowIndexForCellText(0, parentName)
            assertTrue(ind > -1)
            table.selectRow(ind)
        }
    }
}