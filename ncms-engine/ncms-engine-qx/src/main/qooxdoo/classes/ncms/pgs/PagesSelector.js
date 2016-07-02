/**
 * Select pages and assembly(optional)
 */
qx.Class.define("ncms.pgs.PagesSelector", {
    extend: qx.ui.tabview.TabView,

    properties: {

        /**
         * Page selected.
         * Event data: {
         *   id : {Number} Page ID,
         *   name : {String} Page name,
         *   accessMask : {String}
         * }
         */
        selectedPage: {
            check: "Object",
            nullable: true,
            event: "pageSelected"
        },

        /**
         * Assembly selected.
         * Event data: {
         *   id: asm["id"],
         *   template: {Boolean?},  //If assemply is template
         *   assembly: true,        //Assembly data marker
         *   dblClick: {Boolean?}   //Assembly was selected by double click
         * }
         */
        selectedAsm: {
            check: "Object",
            nullable: true,
            event: "asmSelected"
        }
    },

    events: {

        /**
         * Fired if either assembly or page was selected/deselected
         * For data see event data descriptions for:
         * `selectedPage` and `selectedAsm` properties
         */
        "selected": "qx.event.type.Data"
    },

    /**
     * @param allowModify {Boolean?false} Allow CRUD operations on pages
     * @param opts {Map?} Options:
     *                <code>
     *                    {
     *                      asmOpts: {Object} //Assembly selection options, if set the assembly tab will be displayed
     *                      foldersOnly : {Boolean?false} //Show only folders,
     *                      accessAll : {String?} //Optional access all page security restriction
     *                    }
     *                </code>
     */
    construct: function (allowModify, opts) {
        this.base(arguments, "top");
        this._options = opts = opts || {};

        var page;

        if (opts.asmOpts != null) {
            var asmOpts = opts.asmOpts;
            page = new qx.ui.tabview.Page(asmOpts.title || this.tr("Assemblies"));
            page.setLayout(new qx.ui.layout.Grow());
            var as = this._asmSelector =
                new ncms.asm.AsmSelector(
                    asmOpts.constViewSpec || {},
                    new qx.ui.table.selection.Model()
                    .set({selectionMode: qx.ui.table.selection.Model.SINGLE_SELECTION}),
                    asmOpts.useColumns || ["name", "description"]);

            as.getTable().addListener("cellDbltap", this.__onAsmDblClick, this);
            as.addListener("asmSelected", this.__onAsmSelected, this);
            page.add(as);
            this.add(page);
        }

        page = new qx.ui.tabview.Page(this.tr("Structure"));
        page.setLayout(new qx.ui.layout.Grow());
        var ts = this._treeSelector = new ncms.pgs.PagesTreeSelector(allowModify, opts);
        ts.addListener("itemSelected", this.__pageSelected, this);
        page.add(ts);
        this.add(page);

        page = new qx.ui.tabview.Page(this.tr("Search"));
        page.setLayout(new qx.ui.layout.Grow());

        var cvs = {};
        if (opts["foldersOnly"]) {
            cvs["foldersOnly"] = true;
        }
        var ss = this._searchSelector = new ncms.pgs.PagesSearchSelector(cvs, ["icon", "label", "path"]);
        ss.addListener("appear", ss.refresh, ss);
        ss.addListener("itemSelected", this.__pageSelected, this);
        page.add(ss);
        this.add(page);
    },

    members: {

        _asmOpts: null,

        _options: null,

        /**
         * Optonal assembly selector
         */
        _asmSelector: null,

        /**
         * Pages tree navigation selector
         */
        _treeSelector: null,

        /**
         * Pages search selector
         */
        _searchSelector: null,

        __onAsmDblClick: function () {
            this.__asmSelected(this._asmSelector.getSelectedAsm(), true);
        },

        __onAsmSelected: function (ev) {
            this.__asmSelected(ev.getData());
        },

        __asmSelected: function (asm, dblClick) {
            var data = null;
            if (asm != null) {
                data = {
                    id: asm["id"],
                    template: asm["template"],
                    assembly: true,
                    dblClick: !!dblClick
                };
                // Reset selected page if assembly choosen
                this.setSelectedPage(null);
            }
            this.setSelectedAsm(data);
            this.fireDataEvent("selected", data);
        },

        __pageSelected: function (ev) {
            var edata = ev.getData();
            var data = null;
            if (edata != null) {
                data = {
                    id: edata["id"],
                    name: edata["label"],
                    accessMask: edata["accessMask"]
                };
                this.setSelectedAsm(null);
            }
            this.setSelectedPage(data);
            this.fireDataEvent("selected", data);
        },

        getSelectedPageWithExtraInfo: function (cb, cbCtx) {
            var sp = this.getSelectedPage();
            if (sp == null) {
                cb.call(cbCtx, null);
                return;
            }
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.path", {"id": sp["id"]}),
                "GET", "application/json");
            req.send(function (resp) {
                qx.lang.Object.mergeWith(sp, resp.getContent() || {}, true);
                cb.call(cbCtx, sp);
            });
        },

        getAsmSelector: function () {
            return this._asmSelector;
        },

        getTreeSelector: function () {
            return this._treeSelector;
        },

        getSearchSelector: function () {
            return this._searchSelector;
        }
    },

    destruct: function () {
        this._options = null;
        this._treeSelector = null;
        this._searchSelector = null;
        this._asmSelector = null;
        //this._disposeObjects("__field_name");                                
    }
});