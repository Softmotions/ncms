qx.Class.define("ncms.pgs.referrers.PageReferersAttributesTable", {
    extend: sm.table.ToolbarLocalTable,

    properties: {
        "asmId": {
            apply: "__applyAsmId",
            nullable: true,
            check: "Number"
        }
    },

    construct: function (id, title) {
        this.__pageId = id;
        this.__title = title;
        this.base(arguments);
        this.set({allowGrowX: true, allowGrowY: true});
        this._reload([]);
    },

    members: {

        __pageId: null,
        __title: null,

        reload: function () {
            var rid = this.getAsmId();
            this.__applyAsmId(rid);
        },

        __applyAsmId: function (id) {
            var items = [];
            if (id == null) {
                this._reload(items);
                return;
            }
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.referrers.attributes",
                {"guid": this.__pageId, "asmid": id}), "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                data.forEach(function (it) {
                    items.push([[it["type"], it["name"]], it]);
                });
                this._reload(items);
            }, this);

        },

        //overriden
        _createTable: function (tableModel) {
            var table = new sm.table.Table(tableModel, tableModel.getCustom())
            .set({"statusBarVisible": false});
            return table;
        },

        _createToolbarItems: function (toolbar) {
            console.log(this.__title);
            if (this.__title) {
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
                toolbar.add(new qx.ui.basic.Label(this.__title).set({font: "bold", alignY: "middle"}));
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
            }
            console.log(toolbar);
            return toolbar;
        },

        //overriden
        _setJsonTableData: function (tm, items) {
            var data = {
                "columns": [
                    {
                        "title": this.tr("Type").toString(),
                        "id": "type",
                        "sortable": false,
                        "width": 80
                    },
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
        },

        destruct: function () {
            this.__pageId = null;
            this.__title = null;
        }
    }
});