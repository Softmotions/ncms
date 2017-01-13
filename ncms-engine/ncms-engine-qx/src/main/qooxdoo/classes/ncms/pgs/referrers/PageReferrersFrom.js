qx.Class.define("ncms.pgs.referrers.PageReferrersFrom", {
    extend: ncms.pgs.referrers.PageReferrersTab,

    construct: function (item) {
        this.base(arguments,
            item,
            ncms.Application.ACT.getRestUrl("pages.referrers", {guid: item.getGuid()}),
            ncms.Application.ACT.getRestUrl("pages.referrers.count", {id: item.getId()}));
    }
});