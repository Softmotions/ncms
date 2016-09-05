/**
 * User roles table^ set/unset roles and groups for user
 */
qx.Class.define("ncms.usr.UserRolesTable", {
    extend: sm.table.Table,

    construct: function (editable) {
        this.__editable = editable === undefined ? true : !!editable;
        var tm = this._createTableModel();
        this.base(arguments, tm, tm.getCustom());
        this.set({statusBarVisible: false});

        this.addListener("dataEdited", function (ev) {
            if (this.__user == null) {
                return;
            }
            var data = ev.getData();
            var role = tm.getRowAssociatedData(data.row);
            if (role["activeInGroup"]) {
                var tmdata = tm.getData();
                tmdata[data.row][data.col] = data.oldValue;
                tm.setData(tmdata);
                return;
            }
            this.__updateUserRole(this.__user, role, data.value);
        }, this);

        var rr = new sm.table.renderer.CustomRowRenderer();
        rr.setBgColorInterceptor(qx.lang.Function.bind(function (rowInfo) {
            return rowInfo.rowData.rowData["activeInGroup"] ? "#FFFF99" : "white";
        }, this));
        this.setDataRowRenderer(rr);
        this._reload(null, null);
    },

    members: {

        __user: null,

        __editable: null,

        setUser: function (login) {
            if (login == null) {
                this._reload();
                this.__user = null;
                return;
            }

            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user",
                {name: login}), "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                this._reload(data["roles"], data["groups"]);
                this.__user = login;
            }, this);
        },

        _createTableModel: function () {
            var tm = new sm.model.JsonTableModel();
            this._setJsonTableData(tm, null);
            return tm;
        },

        _reload: function (uroles, ugroups) {
            uroles = uroles || [];
            ugroups = ugroups || [];

            var rolesComparator = function (a, b) {
                return a["name"] < b["name"] ? -1 : (a["name"] > b["name"] ? 1 : 0)
            };

            var roles = [];
            var groups = [];

            var req = new sm.io.Request(ncms.Application.ACT.getUrl("security.roles"), "GET", "application/json");
            req.setAsynchronous(false);
            req.send(function (resp) {
                var data = resp.getContent() || [];
                for (var i = 0; i < data.length; ++i) {
                    roles.push(qx.lang.Object.mergeWith(data[i], {type: "role"}));
                }
            }, this);
            roles.sort(rolesComparator);

            req = new sm.io.Request(ncms.Application.ACT.getUrl("security.groups"), "GET", "application/json");
            req.setAsynchronous(false);
            req.send(function (resp) {
                var data = resp.getContent() || [];
                for (var i = 0; i < data.length; ++i) {
                    groups.push(qx.lang.Object.mergeWith(data[i], {type: "group"}));
                }
            }, this);
            groups.sort(rolesComparator);

            var items = [].concat(groups).concat(roles);
            var data = [];
            var dataMap = {};
            var i, dataItem;
            for (i = 0; i < items.length; ++i) {
                var item = items[i];
                dataItem = [[false, item["name"], item["type"], item["description"]], item];
                data[i] = dataItem;
                dataMap[item["name"]] = dataItem; // cache
            }

            // update user roles and groups
            for (i = 0; i < uroles.length; ++i) {
                dataItem = dataMap[uroles[i]];
                if (dataItem) {
                    dataItem[0][0] = dataItem[1]["active"] = true;
                }
            }
            for (i = 0; i < ugroups.length; ++i) {
                var groupItem = dataMap[ugroups[i]];
                if (groupItem) {
                    groupItem[0][0] = groupItem[1]["active"] = true;
                    var groles = groupItem[1]["roles"] || [];
                    for (var j = 0; j < groles.length; ++j) {
                        dataItem = dataMap[groles[j]];
                        if (dataItem) {
                            dataItem[1]["activeInGroup"] = true;
                        }
                    }
                }
            }

            if (this.isEditing()) {
                this.stopEditing();
            }
            this.getSelectionModel().resetSelection();
            this._setJsonTableData(this.getTableModel(), data);

        },

        //overridden
        _setJsonTableData: function (tm, items) {
            var data = {
                "title": "",
                "columns": [
                    {
                        "title": this.tr("Assign").toString(),
                        "id": "active",
                        "type": "boolean",
                        "editable": this.__editable,
                        "width": 75
                    },
                    {
                        "title": this.tr("Role/Group").toString(),
                        "id": "id",
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Type").toString(),
                        "id": "type",
                        "width": 150
                    },
                    {
                        "title": this.tr("Description").toString(),
                        "id": "description",
                        "width": "2*"
                    }
                ],
                "items": items ? items : []
            };
            tm.setJsonData(data);
        },

        __updateUserRole: function (user, data, value) {
            if (user == null) {
                return;
            }

            var params = {name: user};
            params[data["type"]] = data["name"];
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user." + data["type"],
                params), value ? "PUT" : "DELETE", "application/json");
            req.send(function (resp) {
                this.setUser(user);
            }, this);
        }
    },

    destruct: function () {
        this.__user = null;
        this.__editable = null;
    }
});
