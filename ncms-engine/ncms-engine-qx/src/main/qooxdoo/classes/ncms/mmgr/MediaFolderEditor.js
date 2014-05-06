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
    },

    members : {

        __selector : null,

        __editor : null,

        __applyItem : function(item) {
            qx.log.Logger.info("apply item=" + JSON.stringify(item));
            if (item["status"] == 1) { //folder
                this.__selector.setConstViewSpec({"folder" : item["path"]});
            } else {
                this.__selector.setConstViewSpec({});
            }
        }
    },

    destruct : function() {
        this.__selector = null;
        this.__editor = null;
    }
});