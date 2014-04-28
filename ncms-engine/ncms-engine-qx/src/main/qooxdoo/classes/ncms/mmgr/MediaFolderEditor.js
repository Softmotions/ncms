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
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
        this.setBackgroundColor("red");

    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});