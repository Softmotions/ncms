qx.Class.define("ncms.pgs.referrers.PageReferrersTab", {
    extend: qx.ui.core.Widget,

    properties: {
        appearance: {
            refine: true,
            init: "toolbar-table"
        }
    },

    construct: function (item, referrersUrl, referrersCountUrl) {
        this.__item = item;
        this.__referrersUrl = referrersUrl;
        this.__referrersCountUrl = referrersCountUrl;
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        var sp = this.__sp = new qx.ui.splitpane.Pane("vertical");
        this._add(sp);
        this.getChildControl("pages");
    },

    members: {
        __item: null,
        __referrersUrl: null,
        __referrersCountUrl: null,
        __pagesTable: null,
        __attributesTable: null,
        __sp: null,

        _createChildControlImpl: function (id) {
            var control;
            switch (id) {
                case("pages"):
                    control = this.__pagesTable = new ncms.pgs.referrers.PageReferrersSelector(
                        this.__referrersUrl,
                        this.__referrersCountUrl,
                        this.tr("Pages"));
                    control.addListener("pageSelected", this.__onSelectPage, this);
                    this.__sp.add(control, 3);
                    break;
                case("attributes"):
                    control = this.__attributesTable = new ncms.pgs.referrers.PageReferrersAttributesTable(this.tr("Attributes"), this.__item);
                    // this.__attributesTable.setAsmId(this.__item.getId());
                    // this.__attributesTable.setPageId(this.__item.getGuid());
                    this.__sp.add(control, 1);
                    break;
            }
            return control || this.base(arguments, id);
        },

        __onSelectPage: function (e) {
            var data = e.getData();
            if (!data) {
                this._excludeChildControl("attributes");
                return;
            }

            if (!this.hasChildControl("attributes")) {
                this.getChildControl("attributes");
            } else if (!this._isChildControlVisible()) {
                this._showChildControl("attributes");
            }

            this._reloadAttributesTable(data);
        },

        //overriden
        _reloadAttributesTable: function (data) {
            var pageId = data["guid"];
            if (pageId != null) {
                this.__attributesTable.setPageId(pageId);
            }
            var asmId = data["asmid"];
            if (asmId != null) {
                this.__attributesTable.setAsmId(asmId);
            }
        }
    },

    destruct: function () {
        this.__item = null;
        this.__referrersUrl = null;
        this.__referrersCountUrl = null;
        this.__pagesTable = null;
        this.__attributesTable = null;
        this.__sp = null;
    }
});