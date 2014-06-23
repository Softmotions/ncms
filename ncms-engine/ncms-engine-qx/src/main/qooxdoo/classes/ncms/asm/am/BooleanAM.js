/**
 * Boolean checkbox attrubute manager.
 */
qx.Class.define("ncms.asm.am.BooleanAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Checkbox");
        },

        getSupportedAttributeTypes : function() {
            return [ "boolean" ];
        }
    },

    members : {

        __w : null,

        __form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = this._form = new qx.ui.form.Form();
            var cb = this.activateValueEditorWidget(attrSpec, asmSpec);
            form.add(cb, this.tr("Boolean value"), null, "checkbox");
            return new sm.ui.form.FlexFormRenderer(form);
        },

        optionsAsJSON : function() {
            return this.valueAsJSON();
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var val = attrSpec["value"];
            var cb = new qx.ui.form.CheckBox();
            cb.setValue(val != null && (val == "true"));
            this.__w = cb;
            return this.__w;
        },

        valueAsJSON : function() {
            return {
                "value" : this.__w.getValue()
            };
        }
    },

    destruct : function() {
        this.__w = null;
        this._disposeObjects("_form");
    }
});