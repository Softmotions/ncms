/**
 * Generic page editor.
 */
qx.Class.define("ncms.pgs.PageEditor", {
    extend : qx.ui.tabview.TabView,

    statics : {
    },

    events : {
    },

    properties : {

        /**
         * pageSpec:
         * {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         */
        "pageSpec" : {
            check : "Object",
            nullable : true,
            event : "changePage",
            apply : "__applyPageSpec"
        }
    },

    construct : function() {
        this.base(arguments, "top");
        this.set({padding : 5, paddingLeft : 0 });

        var page = this.__createInfoPage();
        if (page != null) {
            this.add(page);
        }
        page = this.__createEditPage();
        if (page != null) {
            this.add(page);
        }
    },

    members : {

        __infoPane : null,

        __editPane : null,

        __createInfoPage : function() {
            var page = new ncms.pgs.PageEditorInfoPage();
            this.bind("pageSpec", page, "pageSpec");
            this.__infoPane = page;
            return page;
        },

        __createEditPage : function() {
            var page = new ncms.pgs.PageEditorEditPage();
            this.bind("pageSpec", page, "pageSpec");
            this.__editPane = page;
            return page;
        },

        __applyPageSpec : function(spec) {
        },

        __populateInfo : function(spec) {

        }
    },

    destruct : function() {
        this.__infoPane = null;
        this.__editPane = null;
        //this._disposeObjects("__field_name");                                
    }
});