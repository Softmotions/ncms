/**
 * Users selector pane.
 */
qx.Class.define("ncms.usr.UsersNav", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {

        /**
         * Event fired if assembly was selected/deselected
         *
         * DATA: var item = {
         *        "id" : {Number} Assembly id.
         *        "name" : {String} User name,
         *        "fullName" : {String} Full user name,
         *       };
         * or null if selection cleared
         */
        "userSelected" : "qx.event.type.Data"
    },

    properties : {

        appearance : {
            refine : true,
            init : "user-selector"
    	},

        constViewSpec : {
            check : "Object",
            nullable : true,
            apply : "__applyConstViewSpec"
        }
    },

    construct : function(constViewSpec, smodel) {
        //todo use example: ncms.asm.AsmSelector
        //todo use sm.ui.form.SearchField as search box
        //todo use ncms.usr.UsersTable (example: ncms.asm.AsmTable)
        //todo use com.softmotions.ncms.security.NcmsSecurityRS service

        this.base(arguments);
        this._setLayout(new qx.ui.layout.Dock());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function() {
            this.__search(null);
        }, this);
        sf.addListener("input", function(ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("changeValue", function(ev) {
            this.__search(ev.getData());
        }, this);


        this.__table = new ncms.usr.UsersTable().set({
            "statusBarVisible" : false,
            "showCellFocusIndicator" : false});

        if (smodel != null) {
            this.__table.setSelectionModel(smodel);
        }
        this.__table.getSelectionModel().addListener("changeSelection", function() {
            var user = this.getSelectedUser();
            this.fireDataEvent("userSelected", user ? user : null);
        }, this);

        this._add(this.__sf, {edge:"north"});
        this._add(this.__table, {edge:"center"});

        this.setConstViewSpec(constViewSpec || null);
    },

    members : {

        /**
         * Search field
         * @type {sm.ui.form.SearchField}
         */
        __sf : null,

        /**
         * Assemblies virtual table
         * @type {ncms.usr.UserTable}
         */
        __table : null,


        setViewSpec : function(vspec) {
            this.__table.getTableModel().setViewSpec(this.__createViewSpec(vspec));
        },

        updateViewSpec : function(vspec) {
            this.__table.getTableModel().updateViewSpec(this.__createViewSpec(vspec));
        },

        reload : function(vspec) {
            this.__table.getTableModel().reloadData();
            this.__table.resetSelection();
        },

        resetSelection : function() {
            this.__table.resetSelection();
        },

        getTable : function() {
            return this.__table;
        },

        getSelectedUserInd : function() {
            return this.__table.getSelectedUserInd();
        },

        getSelectedUser : function() {
            return this.__table.getSelectedUser();
        },

        getSelectedUsers : function() {
            return this.__table.getSelectedUsers();
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

        __search : function(val) {
            this.__table.resetSelection();
            var vspec = (val != null && val != "" ? {stext : val} : {});
            this.setViewSpec(this.__createViewSpec(vspec));
        },

        __applyConstViewSpec : function() {
            this.__search();
        }
    },

    destruct : function() {
        this.__sf = null;
        this.__table = null;
    }
});