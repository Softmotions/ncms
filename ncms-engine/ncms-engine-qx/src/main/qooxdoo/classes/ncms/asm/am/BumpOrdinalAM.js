qx.Class.define("ncms.asm.am.BumpOrdinalAM", {
    extend : ncms.asm.am.BooleanAM,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Bump page ordinal");
        },

        getSupportedAttributeTypes : function() {
            return [ "bump" ];
        },

        isHidden : function() {
            return false;
        }
    },


    members : {

        activateOptionsWidget : function(attrSpec, asmSpec) {
            return null;
        },

        optionsAsJSON : function() {
            return {};
        }
    }
});