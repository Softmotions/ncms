qx.Class.define("ncms.asm.am.SelectAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],


    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Select box");
        },

        getSupportedAttributeTypes : function() {
            return [ "select" ];
        }
    },

    members : {

        activateOptionsWidget : function(attrSpec) {
            return new qx.ui.core.Widget();
        },

        optionsAsJSON : function() {
            return {};
        },

        activateValueEditorWidget : function() {
            return new qx.ui.core.Widget();
        },

        valueAsJSON : function() {
            return {};
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});