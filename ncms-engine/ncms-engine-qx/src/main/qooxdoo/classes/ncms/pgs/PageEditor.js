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
        page = this.__createAccessPane();
        if (page != null) {
            this.add(page);
        }
    },

    members : {

        __infoPane : null,

        __editPane : null,

        __accessPane : null,


        getFirstModifiedPane : function() {
            var editors = this.getChildren();
            for (var i = 0; i < editors.length; ++i) {
                if (editors[i].getModified() === true) {
                    return editors[i];
                }
            }
            return null;
        },

        resetModifiedState : function() {
            var editors = this.getChildren();
            for (var i = 0; i < editors.length; ++i) {
                if (editors[i].getModified() === true) {
                    editors[i].setModified(false);
                }
            }
        },

        __createInfoPage : function() {
            var page = new ncms.pgs.PageEditorInfoPage();
            this.bind("pageSpec", page, "pageSpec");
            this.__infoPane = page;
            return page;
        },

        __createEditPage : function() {
            var page = new ncms.pgs.PageEditorEditPage();
            this.bind("pageSpec", page, "pageSpec");
            page.addListener("modified", this.__onModified, this);
            this.__editPane = page;
            return page;
        },

        __createAccessPane : function() {
            var page = new ncms.pgs.PageEditorAccessPane();
            this.bind("pageSpec", page, "pageSpec");
            page.addListener("modified", this.__onModified, this);
            this.__accessPane = page;
            return page;
        },

        __onModified : function() {
            var awp = ncms.Application.getActiveWorkspace();
            if (awp) {
                awp.setEnabled(this.getFirstModifiedPane() == null);
            }
        },

        __applyPageSpec : function(spec) {
            this.__editPane.setEnabled(false);
            this.__accessPane.setEnabled(false);

            var appState = ncms.Application.APP_STATE;
            var user = appState.getUserLogin();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.check.rights",
                    {pid : spec["id"], user : user, rights : "w"}), "GET", "application/json");
            req.send(function(resp){
                var access = resp.getContent() || false;

                this.__editPane.setEnabled(!!access);
                this.__accessPane.setEnabled(!!access);

                if (!access) {
                    this.setSelection([this.__infoPane]);
                }
            }, this);

        }
    },

    destruct : function() {
        this.__infoPane = null;
        this.__editPane = null;
        this.__accessPane = null;
        //this._disposeObjects("__field_name");                                
    }
});