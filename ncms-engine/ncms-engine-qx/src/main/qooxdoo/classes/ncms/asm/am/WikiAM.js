/**
 * Mediawiki attribute manager.
 */
qx.Class.define("ncms.asm.am.WikiAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "wiki",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Wiki editor");
        },

        getMetaInfo: function () {
            return ncms.asm.am.WikiAM.__META;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = this._form = new sm.ui.form.ExtendedForm();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            el.add(new qx.ui.form.RadioButton(this.tr("mediawiki")).set({"model": "mediawiki"}));
            el.add(new qx.ui.form.RadioButton(this.tr("markdown")).set({"model": "markdown"}));
            el.setModelSelection(opts["markup"] ? [opts["markup"]] : ["mediawiki"]);
            form.add(el, this.tr("Markup language"), null, "markup");
            return new sm.ui.form.FlexFormRenderer(form);
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var res = this._form.populateJSONObject({}, false, true);
            var items = this._form.getItems();
            var sel = items["markup"].getSelection()[0];
            res["markup"] = (sel != null) ? sel.getModel() : "mediawiki";
            return res;
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var w = new ncms.wiki.WikiEditor(attrSpec, asmSpec);
            w.getTextArea().setAutoSize(true);
            w.getTextArea().setMinimalLineHeight(10);
            w.getTextArea().setMaxHeight(600);
            if (opts["markup"] != null) {
                w.setMarkup(opts["markup"])
            }
            var hs = ncms.Application.APP_STATE.getHelpSiteTopicUrl("wiki");
            if (hs == null) {
                hs = ncms.Application.APP_STATE.getHelpSite();
            }
            w.setHelpSite(hs);
            this._fetchAttributeValue(attrSpec, function (val) {
                if (sm.lang.String.isEmpty(val)) {
                    return;
                }
                var spec = JSON.parse(val);
                w.setValue(spec["value"]);
            });
            w.setRequired(!!attrSpec["required"]);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            var w = this._valueWidget;
            return {
                markup: w.getMarkup(),
                value: w.getValue()
            };
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }
});