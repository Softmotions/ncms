/**
 * Media files selector.
 * File list table with search field.
 */
qx.Class.define("ncms.mmgr.MediaFilesSelector", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {

        /**
         * DATA: {
         *   id : {Integer} File ID
         *   name : {String} File name
         *   content_type : {String} File content type
         *   content_length : {Integer} File data length
         *   folder : {String} Full path to file folder
         *   status : {Integer} 1 - folder, 0 - file
         * }
         * or null
         */
        "fileSelected" : "qx.event.type.Data"
    },

    properties : {

        appearance : {
            refine : true,
            init : "mf-selector"
        },

        constViewSpec : {
            check : "Object",
            nullable : true,
            apply : "__applyConstViewSpec"
        }
    },

    construct : function(constViewSpec, smodel) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
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

        this.__table = new ncms.mmgr.MediaFilesTable().set({
            "statusBarVisible" : true,
            "showCellFocusIndicator" : false});

        if (smodel != null) {
            this.__table.setSelectionModel(smodel);
        }
        this.__table.getSelectionModel().addListener("changeSelection", function() {
            var file = this.getSelectedFile();
            this.fireDataEvent("fileSelected", file ? file : null);
        }, this);

        this._add(this.__sf);
        this._add(this.__table, {flex : 1});

        if (constViewSpec != null) {
            this.setConstViewSpec(constViewSpec);
        }
    },

    members : {

        __sf : null,

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