/**
 * Raw attrubute value/settings editor
 */
qx.Class.define("ncms.asm.am.RawAM", {
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
            return [ "raw" ];
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