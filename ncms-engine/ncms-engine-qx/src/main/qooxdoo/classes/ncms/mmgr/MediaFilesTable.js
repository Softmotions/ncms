/**
 *  Virtual table for media files.
 */
qx.Class.define("ncms.mmgr.MediaFilesTable", {
    extend: sm.table.Table,

    construct: function (useColumns, hideColumns, editableColumns, smode) {
        var tm = new sm.model.RemoteVirtualTableModel({
            "name": this.tr("Name"),
            "content_type": this.tr("Type"),
            "content_length": this.tr("Length"),
            "folder": this.tr("Folder"),
            "description": this.tr("Description")
        }, null, this).set({
            "useColumns": useColumns || ["name", "description", "content_type", "content_length"],
            "rowdataUrl": ncms.Application.ACT.getUrl("media.select"),
            "rowcountUrl": ncms.Application.ACT.getUrl("media.select.count")
        });

        var custom = {

            tableColumnModel: function (obj) {
                return new qx.ui.table.columnmodel.Resize(obj);
            },

            selectionModel: function (obj) {
                var res = new sm.table.selection.ExtendedSelectionModel();
                if (smode != null) {
                    res.setSelectionMode(smode);
                }
                return res;
            }
        };

        this.base(arguments, tm, custom);

        var tcm = this.getTableColumnModel();
        var cInd = tm.getColumnIndexById("name");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "3*");
        }
        cInd = tm.getColumnIndexById("content_type");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "1*");
        }
        cInd = tm.getColumnIndexById("content_length");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "1*");
        }
        cInd = tm.getColumnIndexById("description");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "2*");
        }

        hideColumns = hideColumns || ["content_type", "content_length"];
        hideColumns.forEach(function (cn) {
            cInd = tm.getColumnIndexById(cn);
            if (cInd != null) {
                tcm.setColumnVisible(cInd, false);
            }
        }, this);

        editableColumns = editableColumns || [];
        editableColumns.forEach(function (cn) {
            cInd = tm.getColumnIndexById(cn);
            if (cInd != null) {
                tcm.setCellEditorFactory(cInd, new sm.model.TextFieldCellEditor());
                tm.setColumnEditable(cInd, true);
            }
        }, this);
    },

    members: {
        
        getSelectedFileInd: function () {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedFile: function () {
            var sind = this.getSelectedFileInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedFiles: function () {
            var me = this;
            var items = [];
            this.getSelectionModel().iterateSelection(function (ind) {
                var rd = me.getTableModel().getRowData(ind);
                if (rd != null) {
                    items.push(rd);
                }
            });
            return items;
        },

        cleanup: function () {
            this.getTableModel().cleanup();
        }
    },

    destruct: function () {
        //this._disposeObjects("__field_name");
    }
});
