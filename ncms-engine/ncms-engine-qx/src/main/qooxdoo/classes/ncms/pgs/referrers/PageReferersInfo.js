qx.Class.define("ncms.pgs.referrers.PageReferersInfo", {
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
                    control = this.__pagesTable = new ncms.pgs.referrers.PageReferersTable(this.__item)
                    .set({"statusBarVisible": false});
                    control.getSelectionModel().addListener('changeSelection', this.__onSelectPage, this);
                    this.__sp.add(control, 3);
                    break;
                case("attributes"):
                    control = this.__attributesTable = new ncms.pgs.referrers.PageReferersAttributesTable(this.__item.getGuid(), "Attributes");
                    this.__sp.add(control, 1);
                    break;
            }
            return control || this.base(arguments, id);
        },

        __onSelectPage: function (e) {
            if (e.getTarget().getSelectedCount() == 0) {
                this._excludeChildControl("attributes");
                return;
            }

            if (!this.hasChildControl("attributes")) {
                this.getChildControl("attributes");
            } else if (!this._isChildControlVisible()) {
                this._showChildControl("attributes");
            }
            var selectionIndex = e.getTarget().getSelectedRanges()[0].minIndex;
            var asmId = this.__pagesTable.getRowData(selectionIndex)["asmid"];
            if (asmId != null) {
                this.__attributesTable.setAsmId(asmId);
            }
        },

        isAttributesTabShown: function () {
            return this._isChildControlVisible("attributes");
        },

        hideAttributesTab: function () {
            this.__pagesTable.getSelectionModel().resetSelection();
        }
    },

    destruct: function () {
        this.__item = null;
        this.__pagesTable = null;
        this.__attributesTable = null;
        this.__sp = null;
    }
});