qx.Class.define("ncms.pgs.PageReferersInfo", {
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
        this._setLayout(new qx.ui.layout.VBox());
        this.getChildControl("pages");
    },

    members: {
        __pagesTable: null,
        __attributesTable: null,
        __item: null,

        _createChildControlImpl: function (id) {
            var control;
            switch (id) {
                case("pages"):
                    control = this.__pagesTable = new ncms.pgs.PageReferersTable(this.__item);
                    control.getSelectionModel().addListener('changeSelection', this.__onSelectPage, this);
                    this._add(control, {flex: 1});
                    break;
                case("attributes"):
                    control = this.__attributesTable = new ncms.pgs.PageReferersAttributesTable(this.__item.getGuid());
                    this._add(control, {flex: 1});
                    break;
            }
            return control || this.base(arguments, id);
        },

        __onSelectPage: function (e) {
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
            this._excludeChildControl("attributes");
        }
    },

    destruct: function () {
        this.__item = null;
        this.__pagesTable = null;
        this.__attributesTable = null;
    }
});