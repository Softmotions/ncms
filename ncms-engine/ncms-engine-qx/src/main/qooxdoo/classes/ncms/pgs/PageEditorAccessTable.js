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
        this._table.set({statusBarVisible : false});

        // TODO: dataEdited
    },

    members : {

        __applyPageSpec : function(spec) {
            // TODO:
            this._load();
        },

        _load : function() {
            var spec = this.getPageSpec();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl", {id : spec["id"]}), "GET", "application/json");
            req.send(function(resp){
                var data = resp.getContent() || [];
                this._reload(data);
            }, this);
        },

        //overriden
        _createToolbarItems : function(toolbar) {
            // TODO:
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/actions/add.png", this.__addUser, this);
            bt.setToolTipText(this.tr("Add user"));
            part.add(bt);

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
                        "id" : "role.edit",
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
                        "id" : "role.del",
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
        },

        __addUser : function() {
            var spec = this.getPageSpec();
            var dlg = new ncms.usr.UserSelectorDlg();
            dlg.addListener("completed", function(ev) {
                var data = ev.getData()[0];
                dlg.destroy();

                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.acl.add.user", {id : spec["id"], user: data["name"]}), "PUT", "application/json");
                req.send(function(resp){
                    this._load();
                }, this);
            }, this);
            dlg.show();
        }

    },

    destruct : function() {
    }
});
