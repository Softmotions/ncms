/**
 * SocialLinksAM value widget
 */
qx.Class.define("ncms.asm.am.SocialLinksAMValueWidget", {
    extend: qx.ui.core.Widget,
    implement: [qx.ui.form.IModel,
        qx.ui.form.IForm,
        qx.ui.form.IStringForm,
        ncms.asm.am.IValueWidget],
    include: [ncms.asm.am.MValueWidget,
        sm.ui.form.MStringForm],

    properties: {
        model: {
            nullable: true,
            event: "changeModel",
            apply: "__applyModel"
        }
    },

    construct: function (attrSpec, asmSpec) {
        this.__asmSpec = asmSpec;
        this.__attrSpec = attrSpec;
        
        this.base(arguments);

        this._setLayout(new qx.ui.layout.Grow());
        this.addState("widgetNotReady");

        var form = this.__form = new qx.ui.form.Form();

        form.addGroupHeader(this.tr("Social network links"));
        var ph = new qx.ui.form.TextField();
        ph.setPlaceholder(this.tr("Facebook account"));
        form.add(ph, this.tr("Facebook"), null, "facebook");
        ph.addListener("input", this.__modified, this);

        ph = new qx.ui.form.TextField();
        ph.setPlaceholder(this.tr("Twitter account"));
        form.add(ph, this.tr("Twitter"), null, "twitter");
        ph.addListener("input", this.__modified, this);

        ph = new qx.ui.form.TextField();
        ph.setPlaceholder(this.tr("Vkontakte account"));
        form.add(ph, this.tr("Vkontakte"), null, "vkontakte");
        ph.addListener("input", this.__modified, this);

        form.addGroupHeader(this.tr("Social network share buttons"));
        var cb = new qx.ui.form.CheckBox();
        cb.setToolTipText(this.tr("Add facebook share button"));
        cb.addListener("changeValue", this.__modified, this);
        form.add(cb, this.tr("Facebook"), null, "buttonFacebook");

        cb = new qx.ui.form.CheckBox();
        cb.setToolTipText(this.tr("Add twitter share button"));
        cb.addListener("changeValue", this.__modified, this);
        form.add(cb, this.tr("Twitter"), null, "buttonTwitter");

        cb = new qx.ui.form.CheckBox();
        cb.setToolTipText(this.tr("Add vkontakte share button"));
        cb.addListener("changeValue", this.__modified, this);
        form.add(cb, this.tr("Vkontakte"), null, "buttonVkontakte");

        cb = new qx.ui.form.CheckBox();
        cb.setToolTipText(this.tr("Add odnoklassniki share button"));
        cb.addListener("changeValue", this.__modified, this);
        form.add(cb, this.tr("Odnoklassniki"), null, "buttonOdnoklassniki");

        var fr = new sm.ui.form.FlexFormRenderer(form);
        this._add(fr);
    },

    members: {

        __form: null,

        __asmSpec: null,

        __attrSpec: null,
        
        __modified: function () {
            if (this.hasState("widgetNotReady")) {
                return;
            }
            this.fireEvent("modified");
        },

        __applyModel: function (value) {
            this.addState("widgetNotReady");
            var items = this.__form.getItems();
            value = JSON.parse(value);
            var accounts = value["accounts"];
            var buttons = value["buttons"];
            if (accounts["facebook"] != null) {
                items["facebook"].setValue(accounts["facebook"]);
            } else {
                items["facebook"].resetValue();
            }
            if (accounts["twitter"] != null) {
                items["twitter"].setValue(accounts["twitter"]);
            } else {
                items["twitter"].resetValue();
            }
            if (accounts["vkontakte"] != null) {
                items["vkontakte"].setValue(accounts["vkontakte"]);
            } else {
                items["vkontakte"].resetValue();
            }
            items["buttonFacebook"].setValue(buttons["buttonFacebook"] == true);
            items["buttonTwitter"].setValue(buttons["buttonTwitter"] == true);
            items["buttonVkontakte"].setValue(buttons["buttonVkontakte"] == true);
            items["buttonOdnoklassniki"].setValue(buttons["buttonOdnoklassniki"] == true);
            this.removeState("widgetNotReady");
        },

        valueAsJSON: function () {
            var items = this.__form.getItems();
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
        }
    },

    destruct: function () {
        this._disposeObjects("__form");
    }
});
