/**
 * Virtual table of assemblies.
 */
qx.Class.define("ncms.asm.AsmTable", {
    extend : qx.ui.table.Table,

    events : {
    },

    properties : {
    },

    construct : function(useColumns) {
        var tm = new sm.model.RemoteVirtualTableModel({
            "name" : this.tr("Name"),
            "type" : this.tr("Type")
        }).set({
                    "useColumns" : useColumns || ["name", "type"],
                    "rowdataUrl" : ncms.Application.ACT.getUrl("asms.select"),
                    "rowcountUrl" : ncms.Application.ACT.getUrl("asms.select.count")
                });

        var custom = {
            tableColumnModel : function(obj) {
                return new qx.ui.table.columnmodel.Resize(obj);
            }
        };

        this.base(arguments, tm, custom);

        var rr = new sm.table.renderer.CustomRowRenderer();
        rr.setBgColorInterceptor(qx.lang.Function.bind(function(rowInfo) {
            var rdata = rowInfo.rowData;
            //todo row status color assigment
            return "white";
        }, this));
        this.setDataRowRenderer(rr);


        var tcm = this.getTableColumnModel();
        var cInd = tm.getColumnIndexById("name");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "2*");
        }
        cInd = tm.getColumnIndexById("type");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "1*");
        }
    },

    members : {

        getSelectedAsmInd : function() {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedAsm : function() {
            var sind = this.getSelectedAsmInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        cleanup : function() {
            this.getTableModel().cleanup();
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});