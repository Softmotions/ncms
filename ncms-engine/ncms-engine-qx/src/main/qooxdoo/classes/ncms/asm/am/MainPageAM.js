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
        },

        isRequiredSupport: function() {
            return false;
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
            el.setPlaceholder(this.tr("Two letter language codes separated by comma"));
            form.add(el, this.tr("Language codes"), null, "lang");


            el = new qx.ui.form.TextField();
            if (opts["vhost"] != null) {
                el.setValue(opts["vhost"]);
            }
            el.setPlaceholder(this.tr("Comma separated virtual hosts"));
            form.add(el, this.tr("Visrtual hosts"), null, "vhost");

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
            var items = this._form.getItems();
            return {
                "lang": items["lang"].getValue(),
                "vhost": items["vhost"].getValue(),
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