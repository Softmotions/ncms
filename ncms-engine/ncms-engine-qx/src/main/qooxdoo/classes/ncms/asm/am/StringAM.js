/**
 * Simple string attribute manager.
 */
qx.Class.define("ncms.asm.am.StringAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("String attribute manager");
        },

        getSupportedAttributeTypes : function() {
            return [ "string" ];
        }
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);

    },

    members : {


        createOptionsWidget : function(attrSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "green"});
        },

        createValueEditorWidget : function(attrSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "green"});
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});