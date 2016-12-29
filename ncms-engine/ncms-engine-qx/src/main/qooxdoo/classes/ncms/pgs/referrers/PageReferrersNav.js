qx.Class.define("ncms.pgs.referrers.PageReferrersNav", {
    extend: qx.ui.tabview.TabView,

    construct: function (item) {
        this.base(arguments, "top");
        this.setContentPadding(0);
        var page = new qx.ui.tabview.Page(this.tr("from"));
        page.setLayout(new qx.ui.layout.Grow());
        var rw = this.__referrersWith = new ncms.pgs.referrers.PageReferrersFrom(item);
        page.add(rw);
        this.add(page);

        page = new qx.ui.tabview.Page(this.tr("to"));
        page.setLayout(new qx.ui.layout.Grow());
        var rt = this.__referrersTo = new ncms.pgs.referrers.PageReferrersTo(item);
        page.add(rt);
        this.add(page);
    },

    members: {
        __referrersWith: null,
        __referrersTo: null
    },

    destruct: function () {
        this.__referrersTo = null;
        this.__referrersWith = null;
    }
});