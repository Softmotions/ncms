qx.Class.define("ncms.asm.am.MedialineAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Medialine");
        },

        getSupportedAttributeTypes : function() {
            return [ "medialine" ];
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
            var data = {};
            return data;
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new ncms.asm.am.MedialineAMValueWidget(asmSpec, attrSpec);
            this._fetchAttributeValue(attrSpec, function(val) {
                w.setModel((typeof val === "string") ? JSON.parse(val) : null);
            }, this);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON : function() {
            var data = {};
            return data;
        }
    },


    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});