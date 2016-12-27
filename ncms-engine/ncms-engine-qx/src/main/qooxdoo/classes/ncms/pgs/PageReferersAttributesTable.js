qx.Class.define("ncms.pgs.PageReferersAttributesTable", {
    extend: sm.table.Table,

    construct: function (item) {
        var tm = new sm.model.RemoteVirtualTableModel({
            "type": this.tr("type"),
            "name": this.tr("name")
        }).set({
            "useColumns": ["type", "name"],
            "rowdataUrl": ncms.Application.ACT.getRestUrl("pages.referrers.attributes",
                {"guid": item.getGuid(), "asmid": 32}),
            "rowcountUrl": ncms.Application.ACT.getRestUrl("pages.referrers.attributes.count",
                {guid: item.getGuid()})
        });

        var custom = {
            tableColumnModel: function (obj) {
                return new qx.ui.table.columnmodel.Resize(obj);
            }
        };

        tm.setViewSpec({sortInd: 1});
        this.base(arguments, tm.custom);
    }
});