qx.Class.define("ncms.asm.NavAssemblies", {
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
        this._add(new ncms.asm.AsmSelector());
    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});