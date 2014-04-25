/**
 * Assembly parents editor
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 */
qx.Class.define("ncms.asm.AsmAttrsTable", {
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
        this.set({allowGrowX : true, allowGrowY : false, height : 240});
        this._reload([]);
    },

    members : {

        //overriden
        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);
            part.add(this._createButton(null, "ncms/icon/16/actions/add.png"));
            part.add(this._createButton(null, "ncms/icon/16/actions/delete.png"));
            return toolbar;
        },

        _createButton : function(label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance" : "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overriden
        _setJsonTableData : function(tm, items) {
            var data = {
                "title" : "",
                "columns" : [
                    {
                        "title" : "#",
                        "id" : "id",
                        "sortable" : true,
                        "width" : 40
                    },
                    {
                        "title" : this.tr("Name").toString(),
                        "id" : "name",
                        "sortable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Value").toString(),
                        "id" : "value",
                        "sortable" : true,
                        "width" : "3*"
                    },
                    {
                        "title" : this.tr("Type").toString(),
                        "id" : "type",
                        "sortable" : true,
                        "width" : "1*"
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