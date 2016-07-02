/**
 * Main(index) page marker template attribute.
 */
qx.Class.define("ncms.asm.am.MainPageAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Main page marker");
        },

        getSupportedAttributeTypes: function () {
            return ["mainpage"];
        },

        isHidden: function () {
            return true;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = this._form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var el = new qx.ui.form.TextField();
            if (opts["lang"] != null) {
                el.setValue(opts["lang"]);
            }
            el.setPlaceholder(this.tr("Comma separated list of applicable language codes"));
            form.add(el, this.tr("Language codes"), null, "lang");

            el = new qx.ui.form.CheckBox();
            if (opts["enabled"] == "true") {
                el.setValue(true);
            }
            form.add(el, this.tr("Enabled"), null, "enabled");
            return new sm.ui.form.FlexFormRenderer(form);
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            return {
                "lang": this._form.getItems()["lang"].getValue(),
                "enabled": this._form.getItems()["enabled"].getValue()
            };
        },

        activateValueEditorWidget: function () {
            return null;
        },

        valueAsJSON: function () {
            return {}
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }

});