/**
 * Assembly parents editor
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 */
qx.Class.define("ncms.asm.AsmParentsTable", {
    extend: sm.table.ToolbarLocalTable,
    implement: [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include: [
        sm.ui.form.MStringForm,
        sm.table.MTableMutator,
        ncms.cc.MCommands
    ],

    events: {
        "parentsChanged": "qx.event.type.Event"
    },

    construct: function () {
        this.base(arguments);
        this.set({allowGrowX: true, allowGrowY: false, height: 100});
        this._reload([]);
        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__addParents, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__removeParent, this);
    },

    members: {

        __spec: null,

        __delBt: null,

        /**
         */
        setAsmSpec: function (spec) {
            this.__spec = spec || {};
            this.setParentRefs(spec["parentRefs"] || []);
        },

        setParentRefs: function (parentRefs) {
            var items = [];
            parentRefs.forEach(function (el) {
                var ind = el.indexOf(":");
                if (ind === -1) return;
                var id = el.substring(0, ind);
                var name = el.substring(ind + 1);
                var row = [name];
                items.push([row, {"id": id, "name": name}]);
            }, this);
            this._reload(items);
        },

        //overridden
        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/actions/add.png");
            bt.setToolTipText(this.tr("Extend from parent"));
            bt.addListener("execute", this.__addParents, this);
            part.add(bt);

            bt = this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png").set({enabled: false});
            bt.setToolTipText(this.tr("Remove parent"));
            bt.addListener("execute", this.__removeParent, this);
            part.add(bt);

            return toolbar;
        },

        //overridden
        _createTable: function (tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.getSelectionModel().addListener("changeSelection", this._syncState, this);
            table.set({
                showCellFocusIndicator: false,
                statusBarVisible: false,
                focusCellOnPointerMove: false
            });

            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            this._registerCommandFocusWidget(table);
            return table;
        },

        _createButton: function (label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overridden
        _setJsonTableData: function (tm, items) {
            var data = {
                "columns": [
                    {
                        "title": this.tr("Name").toString(),
                        "id": "name",
                        "sortable": false,
                        "width": "1*"
                    }
                ],
                "items": items
            };
            tm.setJsonData(data);
            this._syncState();
        },

        _syncState: function () {
            var ri = this.getSelectedRowIndex();
            this.__delBt.setEnabled(ri != null && ri !== -1);
        },

        __removeParent: function () {
            var spec = this.__spec;
            var asmId = spec["id"];
            var rd = this.getSelectedRowData();
            if (rd == null) {
                return;
            }
            ncms.Application.confirm(
                this.tr("Are you sure to remove parent: \"%1\"?", rd["name"]),
                function (yes) {
                    if (!yes) return;
                    var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("asms.parents", {id: asmId}),
                        "DELETE", "application/json");
                    req.setData(JSON.stringify([rd]));
                    req.setRequestHeader("Content-Type", "application/json");
                    req.send(function (resp) {
                        this.fireEvent("parentsChanged");
                    }, this);
                }, this);
        },

        __addParents: function () {
            var spec = this.__spec;
            var asmName = spec["name"];
            var asmId = spec["id"];
            var dlg = new ncms.asm.AsmSelectorDlg(
                this.tr("Select parents for: %1", asmName),
                null,
                {"notIN": [asmId]}
            );
            dlg.addListener("completed", function (ev) {
                var asms = ev.getData();
                //[{"name":"pub.base","id":1},{"name":"pub.main","id":2}]
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("asms.parents", {id: asmId}),
                    "PUT", "application/json");
                req.setData(JSON.stringify(asms));
                req.send(function (resp) {
                    this.fireEvent("parentsChanged");
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        },

        __beforeContextmenuOpen: function (ev) {
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt = new qx.ui.menu.Button(this.tr("Add parent"));
            bt.addListenerOnce("execute", this.__addParents, this);
            menu.add(bt);
            if (rd != null) {
                bt = new qx.ui.menu.Button(this.tr("Remove parent"));
                bt.addListenerOnce("execute", this.__removeParent, this);
                menu.add(bt);
            }
        }
    },

    destruct: function () {
        this.__spec = null;
        this.__delBt = null;
        //this._disposeObjects("__field_name");
    }
});