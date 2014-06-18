/**
 * Assembly attribute type selector dialog.
 */
qx.Class.define("ncms.asm.AsmAttributeTypeSelectorDlg", {
    extend : qx.ui.window.Window,

    statics : {
    },

    events : {
        /**
         * Data:
         * [type, attribute editor class instance]
         *
         */
        "completed" : "qx.event.type.Data"
    },

    properties : {
    },

    construct : function(caption) {
        this.base(arguments, caption || this.tr("Select assembly attribute type"));
        this.setLayout(new qx.ui.layout.VBox());
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 580
        });

        this.__table = this.__createTable();
        this.add(this.__table, {flex : 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.setEnabled(false);
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);


        this.__closeCmd = new qx.ui.core.Command("Esc");
        this.__closeCmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members : {

        __okBt : null,

        __closeCmd : null,

        __table : null,


        __ok : function() {
            var clazz = this.__table.getSelectedRowData();
            var row = this.__table.getSelectedRowData2();
            if (clazz == null || row == null) {
                return;
            }
            this.fireDataEvent("completed", [row[1], clazz]);
        },

        __createTable : function() {
            var tm = new sm.model.JsonTableModel();

            var items = [];
            ncms.asm.am.AsmAttrManagersRegistry.forEachAttributeManagerTypeClassPair(
                    function(type, clazz) {
                        items.push([
                            [(clazz.getDescription() || ""), type, (clazz.classname || clazz.toString())],
                            clazz
                        ]);
                    }
            );

            this.__setTableData(tm, items);

            var table = new sm.table.Table(tm, tm.getCustom());
            table.addListener("cellDblclick", this.__ok, this);
            table.getSelectionModel().addListener("changeSelection", this.__syncState, this);

            table.set({
                showCellFocusIndicator : false,
                statusBarVisible : true,
                focusCellOnMouseMove : true,
                height : 150,
                allowGrowY : true});
            return table;
        },

        __syncState : function() {
            this.__okBt.setEnabled(!this.__table.getSelectionModel().isSelectionEmpty());
        },

        __setTableData : function(tm, items) {
            items = items || [];
            tm.setJsonData({
                "columns" : [
                    {
                        "title" : this.tr("Description").toString(),
                        "id" : "description",
                        "sortable" : true,
                        "width" : "2*"
                    },
                    {
                        "title" : this.tr("Type").toString(),
                        "id" : "type",
                        "sortable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Class").toString(),
                        "id" : "class",
                        "sortable" : true,
                        "width" : "1*",
                        visible : false
                    }
                ],
                "items" : items
            });
        },


        __dispose : function() {
            this.__table = null;
            this.__okBt = null;
            this._disposeObjects("__closeCmd");
        },

        close : function() {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct : function() {
        this.__dispose();
    }
});
