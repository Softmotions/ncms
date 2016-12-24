/**
 * Markdown attribute manager.
 */
qx.Class.define("ncms.asm.am.MarkdownAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "markdown",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Markdown editor");
        },

        getMetaInfo: function () {
            return ncms.asm.am.MarkdownAM.__META;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = this._form = new sm.ui.form.ExtendedForm();
            return new sm.ui.form.FlexFormRenderer(form);
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var res = this._form.populateJSONObject({}, false, true);
            var items = this._form.getItems();
            return res;
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new qx.ui.core.Widget();
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            var w = this._valueWidget;
            return {
                markup: "",
                value: ""
            };
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }
});
