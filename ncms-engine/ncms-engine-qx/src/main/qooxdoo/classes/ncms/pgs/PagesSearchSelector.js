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
        constViewSpec : {
            check : "Object",
            nullable : true,
            apply : "__applyConstViewSpec"
        }
    },

    construct : function(constViewSpec, allowModify) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function() {
            this.__search(null);
        }, this);
        sf.addListener("input", function(ev) {
            this.__search(ev.getData());
        }, this);
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

        this.setConstViewSpec(constViewSpec || null);

        this._add(this.__sf);
        this._add(this.__table, {flex : 1});
    },

    members : {
        __sf : null,
        __table : null,

        setViewSpec : function(vspec) {
            this.__table.resetSelection();
            this.__table.getTableModel().setViewSpec(this.__createViewSpec(vspec));
        },

        updateViewSpec : function(vspec) {
            this.__table.resetSelection();
            this.__table.getTableModel().updateViewSpec(this.__createViewSpec(vspec));
        },

        __createViewSpec : function(vspec) {
            if (this.getConstViewSpec() == null) {
                return vspec;
            }
            var nspec = {};
            qx.Bootstrap.objectMergeWith(nspec, this.getConstViewSpec(), false);
            qx.Bootstrap.objectMergeWith(nspec, vspec, false);
            return nspec;
        },

        __search : function(val) {
            this.updateViewSpec({name : val || ""});
        },

        __applyConstViewSpec : function() {
            this.__search();
        }
    },

    destruct : function() {
    }
});
