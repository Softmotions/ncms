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
            apply : "_applyPageSpec"
        }
    },

    construct : function() {
        this.base(arguments, "top");
        this.set({padding : 5, paddingLeft : 0 });

        var page = this._createInfoPage();
        if (page != null) {
            this.add(page);
        }
        page = this._createEditPage();
        if (page != null) {
            this.add(page);
        }
        page = this._createAccessPane();
        if (page != null) {
            this.add(page);
        }
    },

    members : {

        _infoPane : null,

        _editPane : null,

        _accessPane : null,


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

        _createInfoPage : function() {
            var page = new ncms.pgs.PageEditorInfoPage();
            this.bind("pageSpec", page, "pageSpec");
            this._infoPane = page;
            return page;
        },

        _createEditPage : function() {
            var page = new ncms.pgs.PageEditorEditPage();
            this.bind("pageSpec", page, "pageSpec");
            page.addListener("modified", this.__onModified, this);
            this._editPane = page;
            return page;
        },

        _createAccessPane : function() {
            var page = new ncms.pgs.PageEditorAccessPane();
            this.bind("pageSpec", page, "pageSpec");
            page.addListener("modified", this.__onModified, this);
            this._accessPane = page;
            return page;
        },

        __onModified : function() {
            var awp = ncms.Application.getActiveWorkspace();
            if (awp) {
                awp.setEnabled(this.getFirstModifiedPane() == null);
            }
        },

        _applyPageSpec : function(spec) {
            if (this._editPane) {
                this._editPane.setEnabled(false);
            }
            if (this._accessPane) {
                this._accessPane.setEnabled(false);
            }
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.check.rights",
                    {pid : spec["id"], rights : "w"}), "GET", "application/json");
            req.send(function(resp) {
                var access = !!resp.getContent();
                if (this._editPane) {
                    this._editPane.setEnabled(access);
                }
                if (this._accessPane) {
                    this._accessPane.setEnabled(access);
                }
                if (!access) {
                    this.setSelection([this._infoPane]);
                }
            }, this);
        }
    },

    destruct : function() {
        this._infoPane = null;
        this._editPane = null;
        this._accessPane = null;
    }
});