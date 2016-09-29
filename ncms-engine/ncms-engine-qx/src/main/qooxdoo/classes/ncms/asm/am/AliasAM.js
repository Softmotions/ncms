/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.asm.am.AliasAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "alias",
            hidden: false,
            requiredSupported: false
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Alias");
        },

        getMetaInfo: function () {
            return ncms.asm.am.AliasAM.__META;
        }
    },

    members: {

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var el = new qx.ui.form.TextField();
            this._fetchAttributeValue(attrSpec, function (val) {
                el.setValue(val);
            });
            form.add(el, this.tr("Alias"), null, "alias");
            var fr = new sm.ui.form.FlexFormRenderer(form);
            fr.setLastRowFlexible();
            this._form = form;
            return fr;
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var items = this._form.getItems();
            return {
                value: items["alias"].getValue()
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new qx.ui.form.TextField();
            this._fetchAttributeValue(attrSpec, function (val) {
                w.setValue(val);
            });
            w.setRequired(!!attrSpec["required"]);
            this._valueWidget = w;
            return w;
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
    }
});