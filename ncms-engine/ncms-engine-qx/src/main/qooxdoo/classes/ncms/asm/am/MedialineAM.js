qx.Class.define("ncms.asm.am.MedialineAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "medialine",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Medialine");
        },

        getMetaInfo: function () {
            return ncms.asm.am.MedialineAM.__META;
        }
    },

    members: {

        __form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var el = new qx.ui.form.TextField();
            el.setRequired(true);
            if (opts["width"] != null) {
                el.setValue(opts["width"]);
            } else {
                el.setValue("800");
            }
            form.add(el, this.tr("Max image width"), sm.util.Validate.canBeRangeNumber(10, 2048, true), "width");

            el = new qx.ui.form.TextField();
            el.setRequired(true);
            if (opts["thumb_width"] != null) {
                el.setValue(opts["thumb_width"]);
            } else {
                el.setValue("96");
            }
            form.add(el, this.tr("Max thumbnail width"), sm.util.Validate.canBeRangeNumber(10, 256, true),
                "thumb_width");

            var fr = new qx.ui.form.renderer.Single(form);
            this.__form = form;
            fr.setAllowGrowX(false);
            return fr;
        },

        optionsAsJSON: function () {
            if (this.__form == null || !this.__form.validate()) {
                return null;
            }
            var items = this.__form.getItems();
            return {
                width: items["width"].getValue(),
                thumb_width: items["thumb_width"].getValue()
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new ncms.asm.am.MedialineAMValueWidget(asmSpec, attrSpec);
            this._fetchAttributeValue(attrSpec, function (val) {
                var model = sm.lang.String.isEmpty(val) ? [] : JSON.parse(val);
                if (!Array.isArray(model)) {
                    model = [];
                }
                //[[964,"корея.jpg","Крыша из кореи"],[966,"желтые цветы.jpg",null],[967,"зеленый цветок.jpg",""]]
                w.setModel(model.map(function (el) {
                    return [
                        [el[1], el[2]],
                        el[0]
                    ];
                }));
            }, this);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            var w = this._valueWidget;
            if (w == null) {
                return null;
            }
            return w.getModel().map(function (el) {
                return [el.rowData].concat(el);
            });
        }
    },

    destruct: function () {
        this._disposeObjects("__form");
    }
});