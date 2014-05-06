/**
 *  Virtual table for media files.
 */
qx.Class.define("ncms.mmgr.MediaFilesTable", {
    extend : sm.table.Table,

    construct : function(useColumns) {
        var tm = new sm.model.RemoteVirtualTableModel({
            "name" : this.tr("Name"),
            "content_type" : this.tr("Type"),
            "content_length" : this.tr("Length"),
            "folder" : this.tr("Folder")
        }).set({
                    "useColumns" : useColumns || ["name", "content_type", "content_length"],
                    "rowdataUrl" : ncms.Application.ACT.getUrl("media.select"),
                    "rowcountUrl" : ncms.Application.ACT.getUrl("media.select.count")
                });
        var custom = {
            tableColumnModel : function(obj) {
                return new qx.ui.table.columnmodel.Resize(obj);
            }
        };
        this.base(arguments, tm, custom, true);

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
    },

    members : {

        getSelectedFileInd : function() {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedFile : function() {
            var sind = this.getSelectedAsmInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedFiles : function() {
            var me = this;
            var asms = [];
            this.getSelectionModel().iterateSelection(function(ind) {
                asms.push(me.getTableModel().getRowData(ind));
            });
            return asms;
        },

        cleanup : function() {
            this.getTableModel().cleanup();
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});