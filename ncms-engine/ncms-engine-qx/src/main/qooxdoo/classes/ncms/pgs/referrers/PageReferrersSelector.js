qx.Class.define("ncms.pgs.referrers.PageReferrersSelector", {
    extend: qx.ui.core.Widget,

    events: {

        /**
         * Event fired if page was selected/deselected
         *
         * DATA: var item = {
         *        "asmid" : {Number} Assembly id.
         *        "name" : {String} Page name,
         *        "path" : {String} Page path,
         *        "icon" : with warn icon if not published
         *       };
         * or null if selection cleared
         */
        pageSelected: "qx.event.type.Data"
    },

    construct: function (dataUrl, countUrl, title) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        this.__table = new ncms.pgs.referrers.PageReferrersTable(
            dataUrl,
            countUrl)
        .set({"statusBarVisible": false});
        this.__table.getSelectionModel().addListener("changeSelection", function () {
            var page = this.getSelectedPage();
            this.fireDataEvent("pageSelected", page ? page : null);
        }, this);
        this.__addCaption(title);
        this._add(this.__table, {flex: 1});

    },

    members: {
        __table: null,

        getSelectedPage: function () {
            return this.__table.getSelectedPage();
        },

        resetSelection: function () {
            this.__table.getSelectionModel().resetSelection();
        },

        __addCaption: function (title) {
            var caption = new qx.ui.toolbar.ToolBar();
            this._add(caption);
            caption.add(new qx.ui.core.Spacer(), {flex: 1});
            caption.add(new qx.ui.basic.Label(title).set({font: "bold", alignY: "middle"}));
            caption.add(new qx.ui.core.Spacer(), {flex: 1});
        }
    },

    destruct: function () {
        this.__table = null;
    }
});