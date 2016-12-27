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
        this.getChildControl("attributes");
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
                    this._add(control, {flex: 1});
                    break;
                case("attributes"):
                    control = this.__attributesTable = new ncms.pgs.PageReferersAttributesTable(this.__item);
                    this._add(control, {flex:1});
                    break;
            }
            return control || this.base(arguments, id);
        }
    }
    ,

    destruct: function () {
        this.__item = null;
        this.__pagesTable = null;
        this.__attributesTable = null;
    }
});