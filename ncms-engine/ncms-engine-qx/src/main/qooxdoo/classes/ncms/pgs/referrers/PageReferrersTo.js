qx.Class.define("ncms.pgs.referrers.PageReferrersTo", {
    extend: qx.ui.core.Widget,

    properties: {
        appearance: {
            refine: true,
            init: "toolbar-table"
        }
    },

    construct: function (item) {
        this.__item = item;
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        var sp = this.__sp = new qx.ui.splitpane.Pane("vertical");
        this._add(sp);
        this.getChildControl("pages");
    },

    members: {
        __pagesTable: null,
        __attributesTable: null,
        __item: null,
        __sp: null,

        _createChildControlImpl: function (id) {
            var control;
            switch (id) {
                case("pages"):
                    control = this.__pagesTable = new ncms.pgs.referrers.PageReferrersSelector(
                        ncms.Application.ACT.getRestUrl("pages.referrers.to", {id: this.__item.getId()}),
                        ncms.Application.ACT.getRestUrl("pages.referrers.to.count", {id: this.__item.getId()}),
                        this.tr("Pages"));
                    control.addListener("pageSelected", this.__onSelectPage, this);
                    this.__sp.add(control, 3);
                    break;
                case("attributes"):
                    control = this.__attributesTable = new ncms.pgs.referrers.PageReferrersAttributesTable(this.tr("Attributes"));
                    this.__attributesTable.setAsmId(this.__item.getId());
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

            var pageId = data["guid"];
            if (pageId != null) {
                this.__attributesTable.setPageId(pageId);
            }
        }
    },

    destruct: function () {
        this.__item = null;
        this.__pagesTable = null;
        this.__attributesTable = null;
        this.__sp = null;
    }
});