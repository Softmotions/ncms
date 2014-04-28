/**
 * Assembly parents editor
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
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
        "parentsChanged" : "qx.event.type.Event"
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

        __spec : null,

        __delBt : null,

        /**
         */
        setAsmSpec : function(spec) {
            this.__spec = spec || {};
            this.setParentRefs(spec["parentRefs"] || []);
        },

        setParentRefs : function(parentRefs) {
            var items = [];
            parentRefs.forEach(function(el) {
                var ind = el.indexOf(":");
                if (ind === -1) return;
                var id = el.substring(0, ind);
                var name = el.substring(ind + 1);
                var row = [id, name];
                items.push([row, {"id" : id, "name" : name}]);
            }, this);
            this._reload(items);
        },

        //overriden
        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/actions/add.png");
            bt.addListener("execute", this.__addParents, this);

            part.add(bt);
            bt = this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png").set({enabled : false});
            bt.addListener("execute", this.__removeParent, this);

            part.add(bt);
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
        },

        __removeParent : function() {
            var spec = this.__spec;
            var asmId = spec["id"];
            var rd = this.getSelectedRowData();
            if (rd == null) return;
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("asms.parents", {id : asmId}),
                    "DELETE", "application/json");
            req.setData(JSON.stringify([rd]));
            req.setRequestHeader("Content-Type", "application/json");
            req.send(function(resp) {
                this.fireEvent("parentsChanged");
            }, this);
        },

        __addParents : function() {
            var spec = this.__spec;
            var asmName = spec["name"];
            var asmId = spec["id"];
            var dlg = new ncms.asm.AsmSelectorDlg(
                    this.tr("Select parents for: ", asmName),
                    null,
                    {"notIN" : [asmId]}
            );
            dlg.addListener("completed", function(ev) {
                var asms = ev.getData();
                //[{"name":"pub.base","id":1},{"name":"pub.main","id":2}]
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("asms.parents", {id : asmId}),
                        "PUT", "application/json");
                req.setData(JSON.stringify(asms));
                req.send(function(resp) {
                    this.fireEvent("parentsChanged");
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        }
    },

    destruct : function() {
        this.__spec = null;
        //this._disposeObjects("__field_name");
    }
});