/**
 * Social links attribute manager.
 */
qx.Class.define("ncms.asm.am.SocLinksAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Social network links");
        },

        getSupportedAttributeTypes: function () {
            return ["soclinks"];
        },

        isHidden: function () {
            return false;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var ph = new qx.ui.form.TextField();
            ph.setPlaceholder(this.tr("Facebook account"));
            if (opts["facebook"] != null) {
                ph.setValue(opts["facebook"]);
            }
            form.add(ph, this.tr("Facebook"), null, "facebook");

            ph = new qx.ui.form.TextField();
            ph.setPlaceholder(this.tr("Twitter account"));
            if (opts["twitter"] != null) {
                ph.setValue(opts["twitter"]);
            }
            form.add(ph, this.tr("Twitter"), null, "twitter");

            ph = new qx.ui.form.TextField();
            ph.setPlaceholder(this.tr("Vkontakte account"));
            if (opts["vkontakte"] != null) {
                ph.setValue(opts["vkontakte"]);
            }
            form.add(ph, this.tr("Vkontakte"), null, "vkontakte");

            var fr = new sm.ui.form.ExtendedDoubleFormRenderer(form);
            this._form = form;
            return fr;
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var items = this._form.getItems();
            return {
                facebook: items["facebook"].getValue(),
                twitter: items["twitter"].getValue(),
                vkontakte: items["vkontakte"].getValue()
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var ph;
            if (opts["facebook"] != null) {
                ph = new qx.ui.form.TextField();
                ph.setValue(opts["facebook"]);
                ph.setPlaceholder(this.tr("Facebook account"));
                form.add(ph, this.tr("Facebook"), null, "facebook");
            }

            if (opts["twitter"] != null) {
                ph = new qx.ui.form.TextField();
                ph.setValue(opts["twitter"]);
                ph.setPlaceholder(this.tr("Twitter account"));
                form.add(ph, this.tr("Twitter"), null, "twitter");
            }

            if (opts["vkontakte"] != null) {
                ph = new qx.ui.form.TextField();
                ph.setValue(opts["vkontakte"]);
                ph.setPlaceholder(this.tr("Vkontakte account"));
                form.add(ph, this.tr("Vkontakte"), null, "vkontakte");
            }

            var fr = new sm.ui.form.ExtendedDoubleFormRenderer(form);
            this._valueWidget = form;
            return fr;
        },

        valueAsJSON: function () {
            if (this._valueWidget == null) {
                return;
            }
            return {
                value: this._valueWidget.getValue()
            }
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }
});
