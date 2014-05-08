/**
 * Show files in media folder
 * and displays/upload file content
 */
qx.Class.define("ncms.mmgr.MediaFolderEditor", {
    extend : qx.ui.core.Widget,

    statics : {

    },

    events : {
    },

    properties : {

        /**
         * Set media item:
         *
         *  item = {
         *        "label"  : {String} Item name.
         *        "status" : {Number} Item status. (1 - folder, 0 - file)
         *        "path"   : {Array} Path to the item (from tree root)
         *  };
         */
        "item" : {
            apply : "__applyItem",
            nullable : true
        }
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());

        var selector = this.__selector = new ncms.mmgr.MediaFilesSelector();
        var editor = this.__editor = new ncms.mmgr.MediaFileEditor();
        selector.bind("fileSelected", editor, "fileSpec");

        var hsp = new qx.ui.splitpane.Pane("horizontal");
        hsp.add(selector, 1);
        hsp.add(editor, 1);
        this._add(hsp);

        this.__dropFun = this.__dropFiles.bind(this);
        this.addListener("appear", function() {
            if (this.getItem() == null) {
                this.__applyItem(null);
            }
            this.__ensureUploadControls();
        }, this);
    },

    members : {

        __selector : null,

        __editor : null,

        __dropFun : null,

        __applyItem : function(item) {
            if (item != null && item["status"] == 1) { //folder
                var folder = "/" + item["path"].join("/");
                this.__selector.setConstViewSpec({"folder" : folder, "status" : 0});
            } else {
                this.__selector.setConstViewSpec({"status" : 0});
            }
        },

        __dropFiles : function(ev) {
            ev.stopPropagation();
            ev.preventDefault();
            var files = ev.dataTransfer.files;
            var path = (this.getItem() != null) ? this.getItem()["path"] : [];
            var dlg = new sm.ui.upload.FileUploadProgressDlg(function(f) {
                return ncms.Application.ACT.getRestUrl("media.upload", path.concat(f.name));
            }, files);
            dlg.addListener("completed", function() {
                dlg.close();
                this.__selector.reload();
            }, this);
            dlg.open();

        },

        __ensureUploadControls : function() {
            var el = this.__selector.getContentElement().getDomElement();
            if (el.ondrop == this.__dropFun) {
                return;
            }
            el.ondrop = this.__dropFun;
            el.ondragover = function() {
                return false;
            };
        }
    },

    destruct : function() {
        if (this.__selector != null && this.__selector.getContentElement() != null) {
            var el = this.__selector.getContentElement().getDomElement();
            el.ondrop = null;
            el.ondragover = null;
        }
        this.__selector = null;
        this.__editor = null;
    }
});