/**
 * Available assemblies selector.
 */
qx.Class.define("ncms.asm.AsmSelector", {
    extend : qx.ui.core.Widget,

    events : {
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

        this.__sf = new sm.ui.form.SearchField();
        this.__table = new ncms.asm.AsmTable();

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
            return this.__stext;
        },

        __createViewSpec : function(vspec) {
            if (this.getConstViewSpec() == null) {
                return vspec;
            }
            var nspec = {};
            qx.lang.Object.carefullyMergeWith(nspec, this.getConstViewSpec());
            qx.lang.Object.carefullyMergeWith(nspec, vspec);
            return nspec;
        }
    },

    destruct : function() {
        this.__sf = null;
        this.__table = null;
    }
});