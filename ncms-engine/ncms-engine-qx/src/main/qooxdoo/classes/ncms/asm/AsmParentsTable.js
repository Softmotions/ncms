/**
 * Assembly parents editor
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 */
qx.Class.define("ncms.asm.AsmParentsTable", {
    extend : sm.table.ToolbarLocalTable,
    implement : [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include : [
        sm.ui.form.MStringForm,
        sm.table.MTableMutator
    ],

    events : {
    },

    properties : {
        "value" : {
            apply : "__applyValue"
        }
    },

    construct : function() {
        this.base(arguments);
        this.set({allowGrowX : true, allowGrowY : false, height : 170});
        this._reload([]);
    },

    members : {

        //overriden
        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part();
            part.setAppearance("toolbar-table/part");
            toolbar.add(part);

            var mb = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/add.png");
            part.add(mb);
            mb = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/delete.png");
            part.add(mb);

            //part.add(new qx.ui.toolbar.Separator());

            mb = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/arrow_up.png");
            part.add(mb);
            mb = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/arrow_down.png");
            part.add(mb);
            return toolbar;
        },

        //overriden
        _setJsonTableData : function(tm, items) {
            var data = {
                "title" : "",
                "columns" : [
                    {
                        "title" : this.tr("#").toString(),
                        "id" : "id",
                        "sortable" : false,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Name").toString(),
                        "id" : "name",
                        "sortable" : false,
                        "width" : "2*"
                    }
                ],
                "items" : items
            };
            tm.setJsonData(data);
        },

        __applyValue : function(val) {
            qx.log.Logger.info("Apply=" + val);
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});