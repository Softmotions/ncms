qx.Class.define("ncms.pgs.referrers.PageReferrersFrom", {
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
                        ncms.Application.ACT.getRestUrl("pages.referrers", {guid: this.__item.getGuid()}),
                        ncms.Application.ACT.getRestUrl("pages.referrers.count", {id: this.__item.getId()}),
                        "Pages");
                    control.addListener("pageSelected", this.__onSelectPage, this);
                    this.__sp.add(control, 3);
                    break;
                case("attributes"):
                    control = this.__attributesTable = new ncms.pgs.referrers.PageReferrersAttributesTable("Attributes");
                    this.__attributesTable.setPageId(this.__item.getGuid());
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

            var asmId = data["asmid"];
            if (asmId != null) {
                this.__attributesTable.setAsmId(asmId);
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