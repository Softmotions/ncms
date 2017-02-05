qx.Class.define("ncms.pgs.referrers.PageReferrersAttributesTable", {
    extend: sm.table.ToolbarLocalTable,

    properties: {
        "pageId": {
            apply: "reload",
            nullable: true,
            check: "String",
            deferredInit:true
        },

        "asmId": {
            apply: "reload",
            nullable: true,
            check: "Number",
            deferredInit:true
        }
    },

    construct: function (title, item) {
        this.__title = title;
        this.base(arguments);
        this.initPageId(item.getGuid());
        this.initAsmId(item.getId());
        this.set({allowGrowX: true, allowGrowY: true});
        this._reload([]);
    },

    members: {

        __title: null,

        reload: function () {
            var items = [];
            console.log("reload");
            if (this.getAsmId() == null || this.getPageId() == null) {
                this._reload(items);
                return;
            }
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.referrers.attributes",
                {"guid": this.getPageId(), "asmid": this.getAsmId()}), "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                data.forEach(function (it) {
                    items.push([[it["type"], it["name"]], it]);
                });
                this._reload(items);
            }, this);
        },

        /**
         * @param tableModel {sm.model.JsonTableModel}
         */
        _createTable: function (tableModel) {
            var table = new sm.table.Table(tableModel, tableModel.getCustom())
            .set({"statusBarVisible": false});
            return table;
        },

        _createToolbarItems: function (toolbar) {
            if (this.__title) {
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
                toolbar.add(new qx.ui.basic.Label(this.__title).set({font: "bold", alignY: "middle"}));
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
            }
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
            this.__title = null;
        }
    }
});