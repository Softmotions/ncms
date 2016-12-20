/**
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.pgs.PageEditorAccessTable", {
    extend: sm.table.ToolbarLocalTable,

    include: [ncms.cc.MCommands],
    
    events: {
        /**
         * Fired when acl for page updated
         */
        "aclUpdated": "qx.event.type.Event"
    },

    properties: {
        /**
         * pageSpec:
         * {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         *
         * @see ncms.pgs.PageEditor
         */
        "pageSpec": {
            check: "Object",
            nullable: true,
            apply: "__applyPageSpec"
        },

        constViewSpec: {
            check: "Object",
            nullable: true,
            apply: "__applyConstViewSpec"
        }
    },

    construct: function (title, constViewSpec) {
        this.base(arguments);

        this.setConstViewSpec(constViewSpec || null);
        this._reload([]);

        if (title) {
            var toolbar = this.getChildControl("toolbar");
            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
            toolbar.add(new qx.ui.basic.Label(title).set({font: "bold", alignY: "middle"}));
            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
        }

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextMenuOpen, this);

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__addUser, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__deleteUser, this);
        this._registerCommandFocusWidget(this._table);
    },

    members: {
        __delBt: null,

        __applyPageSpec: function (spec) {
            this.reload();
        },

        __applyConstViewSpec: function (viewSpec) {
            this.reload();
        },

        __applyConstViewSpecToRequest: function (req) {
            var vspec = this.getConstViewSpec() || {};
            for (var k in vspec) {
                if (vspec[k] != null) {
                    req.setParameter(k, vspec[k], false);
                }
            }
        },

        reload: function () {
            var spec = this.getPageSpec();
            if (!spec) {
                return;
            }

            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl",
                {pid: spec["id"]}), "GET", "application/json");
            this.__applyConstViewSpecToRequest(req);
            req.send(function (resp) {
                var data = resp.getContent() || [];
                this._reload(data);
            }, this);
        },

        //overridden
        _createTable: function (tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.set({statusBarVisible: false});
            table.getSelectionModel().addListener("changeSelection", this._syncState, this);
            table.addListener("dataEdited", this.__dataEdited, this);

            return table;
        },

        //overridden
        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            var bt;

            bt = this._createButton(null, "ncms/icon/16/actions/add.png", this.__addUser, this);
            bt.setToolTipText(this.tr("Add user"));
            part.add(bt);

            this.__delBt = bt = this._createButton(null, "ncms/icon/16/actions/delete.png", this.__deleteUser, this);
            bt.setToolTipText(this.tr("Remove user"));
            part.add(bt);

            return toolbar;
        },

        _syncState: function () {
            var ri = this.getSelectedRowIndex();
            this.__delBt.setEnabled(ri != null && ri !== -1);
        },

        _createButton: function (label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overridden
        _createTableModel: function () {
            var tm = new sm.model.JsonTableModel();
            this._setJsonTableData(tm, null);
            return tm;
        },


        //overridden
        _setJsonTableData: function (tm, data) {
            var items = [];
            data = data || [];
            for (var i = 0; i < data.length; ++i) {
                var item = data[i];
                var am = item["rights"];
                items.push([
                    [item["user"], item["userFullName"], am.indexOf("w") != -1, am.indexOf("n") != -1, am.indexOf(
                        "d") != -1],
                    item
                ]);
            }
            var jdata = {
                "title": "",
                "columns": [
                    {
                        "title": this.tr("Login").toString(),
                        "id": "login",
                        "width": 60
                    },
                    {
                        "title": this.tr("User name").toString(),
                        "id": "name",
                        "width": "2*"
                    },
                    {
                        "title": this.tr("Editing").toString(),
                        "id": "role.write",
                        "type": "boolean",
                        "editable": true,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("News").toString(),
                        "id": "role.news",
                        "type": "boolean",
                        "editable": true,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Deleting").toString(),
                        "id": "role.delete",
                        "type": "boolean",
                        "editable": true,
                        "width": "1*"
                    }
                ],
                "items": items
            };
            tm.setJsonData(jdata);
            this._syncState();
        },

        __addUser: function () {
            this.getTable().cancelEditing();

            var spec = this.getPageSpec();
            var dlg = new ncms.usr.UserSelectorDlg(
                this.tr("Add user to the access list")
            );
            dlg.addListener("completed", function (ev) {
                var data = ev.getData()[0];
                dlg.destroy();

                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.user",
                    {pid: spec["id"], user: data["name"]}), "PUT", "application/json");
                this.__applyConstViewSpecToRequest(req);
                req.send(function (resp) {
                    this.fireEvent("aclUpdated");
                }, this);
            }, this);
            dlg.show();
        },

        __deleteUser: function () {
            this.getTable().cancelEditing();

            var user = this.getSelectedRowData();
            if (!user) {
                return;
            }

            var spec = this.getPageSpec();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.user",
                {pid: spec["id"], user: user["user"]}), "DELETE", "application/json");
            this.__applyConstViewSpecToRequest(req);
            req.send(function (resp) {
                this.fireEvent("aclUpdated");
            }, this);
        },

        __deleteUserRecursive: function () {
            this.getTable().cancelEditing();

            var user = this.getSelectedRowData();
            if (!user) {
                return;
            }

            var spec = this.getPageSpec();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.user",
                {pid: spec["id"], user: user["user"]}), "DELETE", "application/json");
            this.__applyConstViewSpecToRequest(req);
            req.setParameter("forceRecursive", true, false);
            req.send(function (resp) {
                this.fireEvent("aclUpdated");
            }, this);
        },

        __dataEdited: function (ev) {
            var spec = this.getPageSpec();
            var data = ev.getData();
            if (data.value == data.oldValue) {
                return;
            }

            var user = this.getTableModel().getRowData(data.row);
            var parameter = this.getTableModel().getColumnId(data.col);
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.user",
                {pid: spec["id"], user: user.rowData["user"]}), "POST", "application/json");
            this.__applyConstViewSpecToRequest(req);
            req.setParameter("rights", this.__getRoleCharByName(parameter), false);
            req.setParameter("add", data.value, false);
            req.send(function (resp) {
                this.fireEvent("aclUpdated");
            }, this);
        },

        __getRoleCharByName: function (cname) {
            switch (cname) {
                case "role.write":
                    return "w";
                case "role.news":
                    return "n";
                case "role.delete":
                    return "d";
                default :
                    return "";
            }
        },

        __beforeContextMenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var selected = !this.getSelectionModel().isSelectionEmpty();

            var bt = new qx.ui.menu.Button(this.tr("Add user"));
            bt.addListenerOnce("execute", this.__addUser, this);
            menu.add(bt);

            if (selected) {
                bt = new qx.ui.menu.Button(this.tr("Remove user"));
                bt.addListenerOnce("execute", this.__deleteUser, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Remove user recursively"));
                bt.addListenerOnce("execute", this.__deleteUserRecursive, this);
                menu.add(bt);
            }
        }
    },

    destruct: function () {
        this.__delBt = null;
    }
});
