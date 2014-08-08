/**
 * Pages selector with search (plain table)
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.pgs.PagesSearchSelector", {
    extend : qx.ui.core.Widget,

    events : {
        /**
         * DATA: var item = {
         *        "id"     : {Object} Optional Node ID
         *        "label"  : {String} Item name.
         *        "path"   : {String} Path to the item (from tree root)
         *       };
         * or null if selection cleared
         */
        itemSelected : "qx.event.type.Data"
    },

    properties : {

    },

    construct : function(constViewSpec, allowModify) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", this.__search, this);
        sf.addListener("input", this.__search, this);
        sf.addListener("keypress", function(ev) {
            if ("Down" == ev.getKeyIdentifier()) {
                this.__table.handleFocus();
            }
        }, this);

        this.__table = new ncms.pgs.PagesTable().set({
            "statusBarVisible" : false,
            "showCellFocusIndicator" : false});

        this.__table.getSelectionModel().addListener("changeSelection", function() {
            var page = this.__table.getSelectedPage();
            this.fireDataEvent("itemSelected", page ? page : null);
        }, this);

        this._add(this.__sf);
        this._add(this.__table, {flex : 1});

        this.__table.setConstViewSpec(constViewSpec, true);
        this.addListener("appear", this.__search, this);
    },

    members : {
        __sf : null,
        __table : null,

        setViewSpec : function(vspec) {
            this.__table.resetSelection();
            this.__table.setViewSpec(vspec);
        },

        updateViewSpec : function(vspec) {
            this.__table.resetSelection();
            this.__table.updateViewSpec(vspec);
        },

        __search : function() {
            var val = this.__sf.getValue();
            if (!val) {
                this.__table.cleanup();
            } else {
                this.updateViewSpec({name : val || ""});
            }
        }
    },

    destruct : function() {
    }
});
