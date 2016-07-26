/**
 * Virtual host filter.
 */
qx.Class.define("ncms.mtt.filters.MttVHostFilter", {
    extend: qx.core.Object,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Virtual hosts filter");
        },

        getType: function () {
            return "vhosts";
        },

        specForHuman: function (spec) {
            return spec["pattern"] != null ? spec["pattern"] : "";
        }
    },

    members: {

        createWidget: function (spec) {
            var me = this;
            var form = new qx.ui.form.Form();
            var rbg = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            rbg.add(new qx.ui.form.RadioButton(this.tr("Glob")).set({model: "glob"}));
            rbg.add(new qx.ui.form.RadioButton(this.tr("Regexp")).set({model: "regexp"}));
            rbg.setModelSelection(spec["mode"] != null ? [spec["mode"]] : ["glob"]);
            form.add(rbg, this.tr("Mode"), null, "mode");

            var pf = new qx.ui.form.TextField().set({maxLength: 1024});
            pf.setRequired(true);
            if (spec["pattern"] != null) {
                pf.setValue(spec["pattern"]);
            }

            form.add(pf, this.tr("Pattern"), function (value, formItem) {
                if (sm.lang.String.isEmpty(value)) {
                    throw new qx.core.ValidationError("Validation Error",
                        me.tr("This field is required"));
                }
                var rb = rbg.getSelection()[0];
                if (rb != null && rb.getModel() === "regexp") {
                    try {
                        new RegExp(value)
                    } catch (e) {
                        throw new qx.core.ValidationError("Validation Error",
                            me.tr("Not valid regular expression"));
                    }
                }
            }, "pattern");
            return new sm.ui.form.FlexFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            var spec = {};
            var rb = items["mode"].getSelection()[0];
            spec["mode"] = (rb != null) ? rb.getModel() : "glob";
            spec["pattern"] = items["pattern"].getValue();
            return spec;
        }
    },

    destruct: function () {
    }
});