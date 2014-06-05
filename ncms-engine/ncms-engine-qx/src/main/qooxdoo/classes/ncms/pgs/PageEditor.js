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
            var page = new qx.ui.tabview.Page(this.tr("Info"));
            page.setLayout(new qx.ui.layout.VBox());
            return page;
        },

        __createEditPage : function() {
            var page = new qx.ui.tabview.Page(this.tr("Edit"));
            page.setLayout(new qx.ui.layout.VBox(5, "middle"));
            page.add(new qx.ui.basic.Label(this.tr("Loading...")).set({alignX : "center"}));
            page.addListenerOnce("appear", function() {
                qx.log.Logger.info("Edit TAB appeared");
            });
            return page;
        },

        __applyPageSpec : function(spec) {
            qx.log.Logger.info("apply page spec=" + JSON.stringify(spec));
            if (spec == null) {
                return;
            }
        }
    },

    destruct : function() {
        this.__infoPane = null;
        this.__editPane = null;
        //this._disposeObjects("__field_name");                                
    }
});