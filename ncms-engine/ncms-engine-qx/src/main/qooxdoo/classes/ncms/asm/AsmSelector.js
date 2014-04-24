/**
 * Available assemblies selector.
 */
qx.Class.define("ncms.asm.AsmSelector", {
    extend : qx.ui.core.Widget,

    events : {

        /**
         * Event fired if assembly was selected/deselected
         *
         * data: var item = {
         *        "id" : {Number} Assembly id.
         *        "name" : {String} Page name,
         *        "type" : {String} Assembly type,
         *};
         * or null if selection cleared
         */
        "asmSelected" : "qx.event.type.Data"
    },

    properties : {

        appearance : {
            refine : true,
            init : "asm-selector"
        },

        constViewSpec : {
            check : "Object",
            nullable : true
        }
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", this.__search, this);
        sf.addListener("execute", this.__search, this);

        this.__table = new ncms.asm.AsmTable().set({
            "statusBarVisible" : false,
            "showCellFocusIndicator" : false});

        this.__table.getSelectionModel().addListener("changeSelection", function() {
            var asm = this.getSelectedAsm();
            this.fireDataEvent("asmSelected", asm ? asm : null);
        }, this);

        this._add(this.__sf);
        this._add(this.__table, {flex : 1});
        this.updateViewSpec({});
    },

    members : {

        /**
         * Search field
         * @type {sm.ui.form.SearchField}
         */
        __sf : null,

        /**
         * Assemblies virtual table
         * @type {ncms.asm.AsmTable}
         */
        __table : null,


        setViewSpec : function(vspec) {
            this.__table.getTableModel().setViewSpec(this.__createViewSpec(vspec));
        },

        updateViewSpec : function(vspec) {
            this.__table.getTableModel().updateViewSpec(this.__createViewSpec(vspec));
        },

        getTable : function() {
            return this.__table;
        },

        getSelectedAsmInd : function() {
            return this.__table.getSelectedAsmInd();
        },

        getSelectedAsm : function() {
            return this.__table.getSelectedAsm();
        },

        cleanup : function() {
            this.__table.cleanup();
        },

        getSearchField : function() {
            return this.__sf;
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

        __search : function() {
            this.__table.resetSelection();
            var val = this.__sf.getValue();
            var vspec = (val != null && val != "" ? {stext : val} : {});
            qx.log.Logger.info("Search vspec: " + JSON.stringify(vspec));
            this.setViewSpec(this.__createViewSpec(vspec));
        }
    },

    destruct : function() {
        this.__sf = null;
        this.__table = null;
    }
});