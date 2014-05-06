/**
 * Media item editor/info panel
 */
qx.Class.define("ncms.mmgr.MediaFileEditor", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {
    },

    properties : {

        "fileSpec" : {
            check : "Object",
            nullable : true,
            apply : "__applyFileSpec"
        }
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
        //this.setBackgroundColor("green");
    },

    members : {
        __applyFileSpec : function(spec) {
            qx.log.Logger.info("spec=" + JSON.stringify(spec));
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});