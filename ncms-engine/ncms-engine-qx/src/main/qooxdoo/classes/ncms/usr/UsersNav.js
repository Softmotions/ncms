/**
 * Users selector pane.
 */
qx.Class.define("ncms.usr.UsersNav", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.setBackgroundColor("#ffffcc")

    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});