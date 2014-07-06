/**
 * Image attribute manager
 */
qx.Class.define("ncms.asm.am.ImageAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Image");
        },

        getSupportedAttributeTypes : function() {
            return [ "image" ];
        }
    },

    members : {

        _form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = this._form = new qx.ui.form.Form();
            var el = new qx.ui.form.TextField().set({maxLength : 3});
            form.add(el, this.tr("Width"), null, "width");
            el = new qx.ui.form.TextField().set({maxLength : 3});
            form.add(el, this.tr("Height"), null, "height");
            el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Validate sizes"), null, "validate");
            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        optionsAsJSON : function() {
            return {};
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
