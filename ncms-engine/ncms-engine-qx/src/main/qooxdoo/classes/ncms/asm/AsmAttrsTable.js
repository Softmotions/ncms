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
        "attributesChanged" : "qx.event.type.Event"
    },

    properties : {
        "value" : {
            apply : "__applyValue"
        }
    },

    construct : function() {
        this.base(arguments);
        this.set({allowGrowX : true, allowGrowY : false, height : 200});
        this._reload([]);
    },

    members : {

        __spec : null,

        setAsmSpec : function(spec) {
            this.__spec = spec;
            var items = [];
            if (this.__spec == null || !Array.isArray(spec["effectiveAttributes"])) {
                this._reload(items);
                return;
            }
            var attrs = spec["effectiveAttributes"];
            attrs.forEach(function(el) {
                var row = [el["asmId"], el["name"], el["value"], el["type"]];
                items.push([row, el]);
            }, this);

            items.sort(function(o1, o2) {
                var id1 = o1[0][0];
                var id2 = o2[0][0];
                return (id1 < id2) ? -1 : ((id1 === id2) ? 0 : 1);
            });
            this._reload(items);
        },

        //overriden
        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);
            part.add(this._createButton(null, "ncms/icon/16/actions/add.png"));

            this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png");
            part.add(this.__delBt);
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
            this._syncState();
        },

        //overriden
        _createTable : function(tableModel) {
            var table = new sm.table.Table(tableModel, tableModel.getCustom());

            var colorm = qx.theme.manager.Color.getInstance();
            var rr = new sm.table.renderer.CustomRowRenderer();
            rr.setBgColorInterceptor(qx.lang.Function.bind(function(rowInfo) {
                var rdata = rowInfo.rowData;
                if (rdata[0] !== this.__spec["id"]) {
                    return colorm.resolve("table-row-gray");
                } else {
                    return colorm.resolve("background");
                }
            }, this));
            table.setDataRowRenderer(rr);

            table.set({
                showCellFocusIndicator : false,
                statusBarVisible : true,
                focusCellOnMouseMove : false});

            table.getSelectionModel()
                    .addListener("changeSelection", this._syncState, this);
            return table;
        },

        _syncState : function() {
            var rd = this.getSelectedRowData();
            this.__delBt.setEnabled(rd != null && rd["asmId"] == this.__spec["id"]);
        },

        __applyValue : function(val) {
        }
    },

    destruct : function() {
        this.__spec = null;
        //this._disposeObjects("__field_name");
    }
});