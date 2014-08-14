qx.Class.define("ncms.pgs.PagesSelector", {
    extend : qx.ui.tabview.TabView,

    properties : {

        /**
         * Page selected.
         * Data: {
         *   id : {Number} Page ID,
         *   name : {String} Page name,
         *   accessMask : {String}
         * }
         */
        selectedPage : {
            check : "Object",
            nullable : true,
            event : "pageSelected"
        }
    },

    /**
     * @param allowModify {Boolean?false} Allow CRUD operations on pages
     * @param options {Map?} Options:
     *                <code>
     *                    {
     *                      foldersOnly : {Boolean?false} //Show only folders,
     *                      accessAll : {String?} //Optional access all page security restriction
     *                    }
     *                </code>
     */
    construct : function(allowModify, options) {
        this.base(arguments, "top");
        options = options || {};

        var page = new qx.ui.tabview.Page(this.tr("Structure"));
        page.setLayout(new qx.ui.layout.Grow());
        var ts = this._treeSelector = new ncms.pgs.PagesTreeSelector(allowModify, options);
        ts.addListener("itemSelected", this.__pageSelected, this);
        page.add(ts);
        this.add(page);

        page = new qx.ui.tabview.Page(this.tr("Search"));
        page.setLayout(new qx.ui.layout.Grow());

        var cvs = {};
        if (options["foldersOnly"]) {
            cvs["foldersOnly"] = true;
        }
        var ss = this._searchSelector = new ncms.pgs.PagesSearchSelector(cvs);
        ss.addListener("appear", ss.refresh, ss);
        ss.addListener("itemSelected", this.__pageSelected, this);
        page.add(ss);
        this.add(page);
    },

    members : {

        _options : null,

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
                    name : edata["label"],
                    accessMask : edata["accessMask"]
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
        this._options = null;
        this._treeSelector = null;
        this._searchSelector = null;
        //this._disposeObjects("__field_name");                                
    }
});