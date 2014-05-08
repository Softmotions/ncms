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
         *        "path"   : {String} Path to the item (from tree root)
         *  };
         */
        "item" : {
            apply : "__applyItem",
            nullable : false
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
        this.addListener("appear", this.__ensureUploadControls, this);
    },

    members : {

        __selector : null,

        __editor : null,

        __dropFun : null,


        __applyItem : function(item) {
            var folder = "/" + item["path"].join("/");
            if (item["status"] == 1) { //folder
                this.__selector.setConstViewSpec({"folder" : folder, "status" : 0});
            } else {
                this.__selector.setConstViewSpec({"status" : 0});
            }
        },

        __dropFiles : function(ev) {
            qx.log.Logger.info("Files dropped0!");
            ev.stopPropagation();
            ev.preventDefault();
            var files = ev.dataTransfer.files;
            var toUpload = [];
            for (var i = 0, f; f = files[i]; ++i) {
                qx.log.Logger.info("f=" + f.name + " f.type=" + f.type);
            }
            var dlg = new sm.ui.upload.FileUploadProgressDlg(null, toUpload);
            dlg.open();


        },


        __uploadFile : function(f) {

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