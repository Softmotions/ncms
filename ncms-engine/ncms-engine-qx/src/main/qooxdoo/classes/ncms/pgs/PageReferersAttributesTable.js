qx.Class.define("ncms.pgs.PageReferersAttributesTable", {
    extend: sm.table.ToolbarLocalTable,

    properties: {
        "asmId": {
            apply: "__applyAsmId",
            nullable: true,
            check: "Number"
        }
    },

    construct: function (id) {
        this.base(arguments);
        this.set({allowGrowX: true, allowGrowY: true});
        this._reload([]);
        this.__pageId = id;
    },

    members: {

        __pageId: null,

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

        //ovrriden
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
        }
    }
});