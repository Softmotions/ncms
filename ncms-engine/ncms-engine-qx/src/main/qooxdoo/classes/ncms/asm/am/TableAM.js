/**
 * Table value widget
 */
qx.Class.define("ncms.asm.am.TableAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],


    statics: {

        __META: {
            attributeTypes: "table",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Table");
        },

        getMetaInfo: function () {
            return ncms.asm.am.TableAM.__META;
        }
    },

    members: {

        activateOptionsWidget: function (attrSpec, asmSpec) {
            return this.activateValueEditorWidget(attrSpec, asmSpec);
        },

        optionsAsJSON: function () {
            return this.valueAsJSON();
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new ncms.asm.am.TableAMValueWidget();
            this._fetchAttributeValue(attrSpec, function (val) {
                try {
                    w.setModel(sm.lang.String.isEmpty(val) ? [] : JSON.parse(val));
                } catch (e) {
                    qx.log.Logger.error("Failed to apply table value", e);
                }
            });
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            var w = this._valueWidget;
            return (w != null) ? w.valueAsJSON() : [];
        }
    }
});