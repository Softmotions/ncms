/**
 * Social links attribute manager.
 */
qx.Class.define("ncms.asm.am.SocialLinksAM", {
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
            var accounts = [];
            var buttons = [];
            var value = [];
            this._fetchAttributeValue(attrSpec, function (val) {
                value = JSON.parse(val);
                accounts = value["accounts"];
                buttons = value["buttons"];
            });
            
            form.addGroupHeader(this.tr("Social network links"));
            var ph = new qx.ui.form.TextField();
            ph.setPlaceholder(this.tr("Facebook account"));
            if (accounts["facebook"] != null) {
                ph.setValue(accounts["facebook"]);
            }
            form.add(ph, this.tr("Facebook"), null, "facebook");

            ph = new qx.ui.form.TextField();
            ph.setPlaceholder(this.tr("Twitter account"));
            if (accounts["twitter"] != null) {
                ph.setValue(accounts["twitter"]);
            }
            form.add(ph, this.tr("Twitter"), null, "twitter");

            ph = new qx.ui.form.TextField();
            ph.setPlaceholder(this.tr("Vkontakte account"));
            if (accounts["vkontakte"] != null) {
                ph.setValue(accounts["vkontakte"]);
            }
            form.add(ph, this.tr("Vkontakte"), null, "vkontakte");

            form.addGroupHeader(this.tr("Social network share buttons"));
            var cb = new qx.ui.form.CheckBox();
            cb.setValue(buttons["buttonFacebook"] == true);
            cb.setToolTipText(this.tr("Add facebook share button"));
            form.add(cb, this.tr("Facebook"), null, "buttonFacebook");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(buttons["buttonTwitter"] == true);
            cb.setToolTipText(this.tr("Add twitter share button"));
            form.add(cb, this.tr("Twitter"), null, "buttonTwitter");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(buttons["buttonVkontakte"] == true);
            cb.setToolTipText(this.tr("Add vkontakte share button"));
            form.add(cb, this.tr("Vkontakte"), null, "buttonVkontakte");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(buttons["buttonOdnoklassniki"] == true);
            cb.setToolTipText(this.tr("Add odnoklassniki share button"));
            form.add(cb, this.tr("Odnoklassniki"), null, "buttonOdnoklassniki");

            var fr = new sm.ui.form.FlexFormRenderer(form);
            this._form = form;
            return fr;
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var items = this._form.getItems();
            return {
                value: {
                    accounts: {
                        facebook: items["facebook"].getValue(),
                        twitter: items["twitter"].getValue(),
                        vkontakte: items["vkontakte"].getValue()
                    },
                    buttons: {
                        buttonFacebook: items["buttonFacebook"].getEnabled() ? items["buttonFacebook"].getValue() : false,
                        buttonTwitter: items["buttonTwitter"].getEnabled() ? items["buttonTwitter"].getValue() : false,
                        buttonVkontakte: items["buttonVkontakte"].getEnabled() ? items["buttonVkontakte"].getValue() : false,
                        buttonOdnoklassniki: items["buttonOdnoklassniki"].getEnabled() ? items["buttonOdnoklassniki"].getValue() : false
                    }
                }
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new ncms.asm.am.SocialLinksAMValueWidget(attrSpec, asmSpec);
            this._fetchAttributeValue(attrSpec, function (val) {
                w.setModel(val);
            });
            w.setRequired(!!attrSpec["required"]);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            return this._valueWidget.valueAsJSON();
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }
});
