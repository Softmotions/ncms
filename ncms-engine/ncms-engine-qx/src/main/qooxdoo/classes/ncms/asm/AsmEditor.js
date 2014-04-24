/**
 * Assembly instance editor.
 */
qx.Class.define("ncms.asm.AsmEditor", {
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
        this.setBackgroundColor("yellow");
    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});