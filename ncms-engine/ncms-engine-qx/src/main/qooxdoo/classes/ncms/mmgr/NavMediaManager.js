/**
 * Media folders tree
 */
qx.Class.define("ncms.mmgr.NavMediaManager", {
    extend : qx.ui.core.Widget,

    statics : {
        MMF_EDITOR_CLAZZ : "ncms.mmgr.MediaFolderEditor"
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
        this.setBackgroundColor("green");

        //Register media folder editor
        var eclazz = ncms.mmgr.NavMediaManager.MMF_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.mmgr.MediaFolderEditor();
        }, null, this);

        this.addListener("disappear", function() {
            //Navigation side is inactive so hide mmfolder editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
        }, this);
        this.addListener("appear", function() {
            if (app.getActiveWSAID() != eclazz) {
                app.showWSA(eclazz);
            }
        }, this);
    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});