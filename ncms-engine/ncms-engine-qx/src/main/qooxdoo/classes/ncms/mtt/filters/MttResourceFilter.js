/**
 * Resource prefix filter.
 */
qx.Class.define("ncms.mtt.filters.MttResourceFilter", {
    extend: qx.core.Object,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Resource prefix filter");
        },

        getType: function () {
            return "resource";
        },

        specForHuman: function (spec) {
            return spec["prefix"] != null ? spec["prefix"] : "";
        }
    },

    members: {

        createWidget: function (spec) {
            var form = new qx.ui.form.Form();
            var pf = new qx.ui.form.TextField().set({maxLength: 1024});
            pf.setRequired(true);
            pf.setPlaceholder(this.tr("This field is required"));
            if (spec["prefix"] != null) {
                pf.setValue(spec["prefix"]);
            }
            form.add(pf, this.tr("Prefix"), null, "prefix");
            return new sm.ui.form.FlexFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            var spec = {};
            spec["prefix"] = items["prefix"].getValue();
            return spec;
        }
    },

    destruct: function () {
    }
});
