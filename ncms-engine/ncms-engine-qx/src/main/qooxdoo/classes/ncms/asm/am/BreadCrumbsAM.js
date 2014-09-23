qx.Class.define("ncms.asm.am.BreadCrumbsAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Breadcrumbs");
        },

        getSupportedAttributeTypes : function() {
            return [ "breadcrumbs" ];
        },

        isHidden : function() {
            return true;
        }
    },


    members : {

        activateOptionsWidget : function() {
            return null;
        },

        optionsAsJSON : function() {
            return {};
        },

        activateValueEditorWidget : function() {
            return null;
        },

        valueAsJSON : function() {
            return {}
        }
    }
});