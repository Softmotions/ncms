qx.Class.define("ncms.asm.am.DateAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Date selector");
        },

        getSupportedAttributeTypes: function () {
            return ["date"];
        },

        isHidden: function () {
            return false;
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
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var w = new qx.ui.form.DateField();
            if (!sm.lang.String.isEmpty(opts["format"])) {
                w.setDateFormat(new qx.util.format.DateFormat(opts["format"]));
            }
            this._fetchAttributeValue(attrSpec, function (val) {
                val = (val != null) ? Number(val) : NaN;
                w.setValue(isNaN(val) ? new Date() : new Date(val));
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
            var date = this._valueWidget.getValue();
            return {
                value: (date != null) ? +date : null
            }
        }
    },

    destruct: function () {
        this._disposeObjects("__form");
    }
});