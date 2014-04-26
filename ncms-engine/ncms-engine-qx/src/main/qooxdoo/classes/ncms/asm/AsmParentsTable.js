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
        this.set({allowGrowX : true, allowGrowY : false, height : 100});
        this._reload([]);
    },

    members : {

        __delBt : null,

        /**
         * spec example: ["2:pub.main", 4:pub.common],
         */
        setParentRefs : function(spec) {
            var items = [];
            if (!Array.isArray(spec)) {
                this._reload(items);
                return;
            }
            spec.forEach(function(el) {
                var ind = el.indexOf(":");
                if (ind === -1) return;
                var row = [el.substring(0, ind), el.substring(ind + 1)];
                items.push([row, spec]);
            }, this);
            this._reload(items);
        },

        //overriden
        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);
            part.add(this._createButton(null, "ncms/icon/16/actions/add.png"));
            this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png").set({enabled : false});
            part.add(this.__delBt);
            return toolbar;
        },

        //overriden
        _createTable : function(tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.getSelectionModel().addListener("changeSelection", this._syncState, this);
            table.set({
                showCellFocusIndicator : false,
                statusBarVisible : false,
                focusCellOnMouseMove : false});
            return table;
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
                        "title" : this.tr("#").toString(),
                        "id" : "id",
                        "sortable" : false,
                        "width" : 40
                    },
                    {
                        "title" : this.tr("Name").toString(),
                        "id" : "name",
                        "sortable" : false,
                        "width" : "1*"
                    }
                ],
                "items" : items
            };
            tm.setJsonData(data);
            this._syncState();
        },

        _syncState : function() {
            var ri = this.getSelectedRowIndex();
            this.__delBt.setEnabled(ri != null && ri !== -1);
        },

        __applyValue : function(val) {
            this.setParentRefs(JSON.parse(val));
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});