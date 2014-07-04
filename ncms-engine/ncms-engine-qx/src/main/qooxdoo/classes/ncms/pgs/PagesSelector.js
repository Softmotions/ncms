qx.Class.define("ncms.pgs.PagesSelector", {
    extend : qx.ui.tabview.TabView,

    statics : {
    },

    events : {

    },

    properties : {

        /**
         * Page selected.
         * Data: {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         */
        selectedPage : {
            check : "Object",
            nullable : true,
            event : "pageSelected"
        }
    },

    construct : function(allowModify) {
        this.base(arguments, "top");

        var page = new qx.ui.tabview.Page(this.tr("Structure"));
        page.setLayout(new qx.ui.layout.Grow());
        var ts = this._treeSelector = new ncms.pgs.PagesTreeSelector(allowModify);
        ts.addListener("itemSelected", this.__pageSelected, this);
        page.add(ts);
        this.add(page);

        page = new qx.ui.tabview.Page(this.tr("Search"));
        page.setLayout(new qx.ui.layout.Grow());

        //todo rows loaded when tab is not actually opened
        var ss = this._searchSelector = new ncms.pgs.PagesSearchSelector(null, allowModify);
        ss.addListener("itemSelected", this.__pageSelected, this);
        page.add(ss);
        this.add(page);
    },

    members : {

        /**
         * Pages tree navigation selector
         */
        _treeSelector : null,

        /**
         * Pages search selector
         */
        _searchSelector : null,

        __pageSelected : function(ev) {
            var edata = ev.getData();
            var data = null;
            if (edata != null) {
                data = {
                    id : edata["id"],
                    name : edata["label"]
                }
            }
            this.setSelectedPage(data);
        },

        getSelectedPageWithExtraInfo : function(cb, cbCtx) {
            var sp = this.getSelectedPage();
            if (sp == null) {
                cb.call(cbCtx, null);
                return;
            }
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.path", {"id" : sp["id"]}),
                    "GET", "application/json");
            req.send(function(resp) {
                qx.lang.Object.mergeWith(sp, resp.getContent() || {}, true);
                cb.call(cbCtx, sp);
            });
        }
    },

    destruct : function() {
        this._treeSelector = null;
        this._searchSelector = null;
        //this._disposeObjects("__field_name");                                
    }
});