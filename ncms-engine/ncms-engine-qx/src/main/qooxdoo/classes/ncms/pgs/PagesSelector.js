qx.Class.define("ncms.pgs.PagesSelector", {
    extend : qx.ui.tabview.TabView,

    statics : {
    },

    events : {

        /**
         * Data:
         * {
         *    id : {Number} Page ID,
         *    name : {String} Page name,
         * }
         */
        "pageSelected" : "qx.event.type.Data"
    },

    properties : {
    },

    construct : function(allowModify) {
        this.base(arguments, "top");

        var page = new qx.ui.tabview.Page(this.tr("Structure"));
        page.setLayout(new qx.ui.layout.Grow());
        var ts = this._treeSelector = new ncms.pgs.PagesTreeSelector(allowModify);
        page.add(ts);
        this.add(page);

        page = new qx.ui.tabview.Page(this.tr("Search"));
        page.setLayout(new qx.ui.layout.Grow());
        this.add(page);
    },

    members : {

        /**
         * Pages tree navigation selector
         */
        _treeSelector : null,

        /**
         * Pages search form
         */
        _searchSelector : null

    },

    destruct : function() {
        this._treeSelector = null;
        this._searchSelector = null;
        //this._disposeObjects("__field_name");                                
    }
});