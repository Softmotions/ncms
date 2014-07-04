/**
 * Markdown/Mediawiki attribute manager.
 */
qx.Class.define("ncms.asm.am.WikiAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Wiki editor");
        },

        getSupportedAttributeTypes : function() {
            return [ "wiki" ];
        }
    },

    members : {

        _form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "red"});
        },

        optionsAsJSON : function() {
            return this._form.populateJSONObject({}, false, true);
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "green"});
        },

        valueAsJSON : function() {
            return {};
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});