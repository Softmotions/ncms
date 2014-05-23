/**
 * Simple string attribute manager.
 */
qx.Class.define("ncms.asm.am.StringAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],

    statics : {
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);

    },

    members : {

        getSupportedAttributeTypes : function() {
            return [ "string" ];
        },

        createOptionsWidget : function(attrSpec) {

        },

        createValueEditorWidget : function(attrSpec) {

        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});