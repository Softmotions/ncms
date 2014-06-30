/**
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.pgs.PageEditorAccessTable", {
    extend : sm.table.ToolbarLocalTable,

    events : {
    },

    properties : {
        /**
         * pageSpec:
         * {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         *
         * @see ncms.pgs.PageEditor
         */
        "pageSpec" : {
            check : "Object",
            nullable : true,
            apply : "__applyPageSpec"
        }
    },

    construct : function() {
        this.base(arguments);

        this._reload([]);
    },

    members : {
        __delBt : null,

        __applyPageSpec : function(spec) {
            this._load();
        },

        _load : function() {
            var spec = this.getPageSpec();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl", {pid : spec["id"]}), "GET", "application/json");
            req.send(function(resp){
                var data = resp.getContent() || [];
                this._reload(data);
            }, this);
        },

        //overriden
        _createTable : function(tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.set({statusBarVisible : false});
            table.getSelectionModel().addListener("changeSelection", this._syncState, this);
            table.addListener("dataEdited", this.__dataEdited, this);

            return table;
        },

        //overriden
        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            var bt;

            bt = this._createButton(null, "ncms/icon/16/actions/add.png", this.__addUser, this);
            bt.setToolTipText(this.tr("Add user"));
            part.add(bt);

            this.__delBt = bt = this._createButton(null, "ncms/icon/16/actions/delete.png", this.__deleteUser, this);
            bt.setToolTipText(this.tr("Delete user"));
            part.add(bt);

            return toolbar;
        },

        _syncState : function() {
            var ri = this.getSelectedRowIndex();
            this.__delBt.setEnabled(ri != null && ri !== -1);
        },

        _createButton : function(label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance" : "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overriden
        _createTableModel : function() {
            var tm = new sm.model.JsonTableModel();
            this._setJsonTableData(tm, null);
            return tm;
        },


        //overriden
        _setJsonTableData : function(tm, data) {
            var items = [];
            data = data || [];
            for(var i = 0; i < data.length; ++i) {
                var item = data[i];
                var am = item["rights"];
                items.push([
                    [item["user"], item["userFullName"], am.indexOf("w") != -1, am.indexOf("n") != -1, am.indexOf("d") != -1, item["recursive"] == 1],
                    item
                ]);
            }
            var jdata = {
                "title" : "",
                "columns" : [
                    {
                        "title" : this.tr("Login").toString(),
                        "id" : "login",
                        "width" : 60
                    },
                    {
                        "title" : this.tr("User name").toString(),
                        "id" : "name",
                        "width" : "2*"
                    },
                    {
                        "title" : this.tr("Editing").toString(),
                        "id" : "role.write",
                        "type" : "boolean",
                        "editable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("News").toString(),
                        "id" : "role.news",
                        "type" : "boolean",
                        "editable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Deleting").toString(),
                        "id" : "role.delete",
                        "type" : "boolean",
                        "editable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Recursive").toString(),
                        "id" : "role.recursive",
                        "type" : "boolean",
                        "editable" : true,
                        "width" : "1*"
                    }
                ],
                "items" : items
            };
            tm.setJsonData(jdata);
            this._syncState();
        },

        __addUser : function() {
            this.getTable().cancelEditing();

            var spec = this.getPageSpec();
            var dlg = new ncms.usr.UserSelectorDlg();
            dlg.addListener("completed", function(ev) {
                var data = ev.getData()[0];
                dlg.destroy();

                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.user", {pid : spec["id"], user: data["name"]}), "PUT", "application/json");
                req.send(function(resp){
                    this._load();
                }, this);
            }, this);
            dlg.show();
        },

        __deleteUser : function() {
            this.getTable().cancelEditing();

            var user = this.getSelectedRowData();
            if (!user) {
                return;
            }

            var spec = this.getPageSpec();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.user", {pid : spec["id"], user: user["user"]}), "DELETE", "application/json");
            req.setParameter("recursive", user["recursive"] == 1, false);
            req.send(function(resp){
                this._load();
            }, this);
        },

        __dataEdited : function(ev) {
            var spec = this.getPageSpec();
            var data = ev.getData();
            if (data.value == data.oldValue) {
                return;
            }

            var user = this.getTableModel().getRowData(data.row);
            var parameter = this.getTableModel().getColumnId(data.col);
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.user", {pid : spec["id"], user: user.rowData["user"]}), "POST", "application/json");
            req.setParameter("recursive", user.rowData["recursive"] == 1, true);
            if (parameter == "role.recursive") {
                req.setParameter(parameter, data.value, true);
            } else {
                req.setParameter("rights", this.__populateUserRights(user.rowData, parameter, data.value), true);
            }
            req.send(function(resp){
                this._load();
            }, this);
        },

        __populateUserRights : function(user, role, isset) {
            var am = user["rights"];
            var roleChar = this.__getRoleCharByName(role);
            if (roleChar == "") {
                return am;
            } else if (isset) {
                return am.indexOf(roleChar) != -1 ? am : am + roleChar;
            } else {
                return am.replace(roleChar, "");
            }
        },

        __getRoleCharByName : function(cname) {
            switch (cname) {
                case "role.write": return "w";
                case "role.news": return "n";
                case "role.delete": return "d";
                default : return "";
            }
        }
    },

    destruct : function() {
        this.__delBt = null;
    }
});
