qx.Class.define("ncms.asm.am.DateAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META:  {
            attributeTypes: "date",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Date selector");
        },

        getMetaInfo: function () {
            return ncms.asm.am.DateAM.__META;
        }
    },

    members: {

        __form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var el = new qx.ui.form.TextField();
            if (opts["format"] != null) {
                el.setValue(opts["format"]);
            }
            form.add(el, this.tr("Date format"), null, "format");
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
                format: items["format"].getValue()
            }
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var isoDateFormat = new qx.util.format.DateFormat("isoDateTime");
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var w = new qx.ui.form.DateField();
            if (!sm.lang.String.isEmpty(opts["format"])) {
                w.setDateFormat(new qx.util.format.DateFormat(opts["format"]));
                w.setPlaceholder(opts["format"]);
            }
            this._fetchAttributeValue(attrSpec, function (val) {
                if (!sm.lang.String.isEmpty(val)) {
                    try {
                        w.setValue(isoDateFormat.parse(val));
                    } catch (e) {
                        console.log("Error parsing iso date: " + val);
                    }
                } else {
                    w.setValue(new Date());
                }
            });
            w.setRequired(!!attrSpec["required"]);
            w.setWidth(150);
            w.setAllowGrowX(false);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            if (this._valueWidget == null) {
                return;
            }
            var date = this._valueWidget.getValue() || new Date();
            // Local ISO date-time format
            // 2007-12-03T10:15:30
            var isoDateFormat = new qx.util.format.DateFormat("isoDateTime");
            return {
                value: isoDateFormat.format(date)
            }
        }
    },

    destruct: function () {
        this._disposeObjects("__form");
    }
});