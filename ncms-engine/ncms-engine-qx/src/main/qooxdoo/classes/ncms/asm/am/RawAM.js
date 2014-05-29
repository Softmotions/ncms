/**
 * Raw attrubute value/settings editor
 */
qx.Class.define("ncms.asm.am.RawAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Raw attribute data manager");
        },


        getSupportedAttributeTypes : function() {
            return [ "raw" ];
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
            return new qx.ui.core.Widget().set({backgroundColor : "red"});
        },

        optionsAsJSON : function(optionsWidget, attrSpec) {
            return {};
        },

        createValueEditorWidget : function(attrSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "red"});
        },

        valueAsJSON : function(valueWidget, attrSpec) {
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});