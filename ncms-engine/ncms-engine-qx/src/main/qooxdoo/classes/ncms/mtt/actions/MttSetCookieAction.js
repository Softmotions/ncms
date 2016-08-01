/**
 * Mtt set cookie action
 */
qx.Class.define("ncms.mtt.actions.MttSetCookieAction", {
    extend: qx.core.Object,
    implement: [ncms.mtt.actions.IMttAction],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Set a cookie");
        },

        getType: function () {
            return "cookie";
        },

        specForHuman: function (spec) {
            return spec["name"] + "=" + spec["value"] +
                "," + spec["units"] + "=" + spec["time"];
        }
    },

    members: {

        createWidget: function (spec) {
            var form = new qx.ui.form.Form();
            var time = new qx.ui.form.Spinner(0, 1).set({required: true});
            if (spec["time"]) {
                time.setValue(spec["time"]);
            }
            form.add(time, this.tr("Lifetime"), null, "time", null, {flex: 1});

            var units = new qx.ui.form.SelectBox();
            units.add(new qx.ui.form.ListItem(this.tr("Days"), null, "days"));
            units.add(new qx.ui.form.ListItem(this.tr("Minutes"), null, "minutes"));
            if (spec["units"]) {
                units.setModelSelection([spec["units"]]);
            }
            form.add(units, "", null, "units");

            var name = new qx.ui.form.TextField().set({required: true});
            if (spec["name"]) {
                name.setValue(spec["name"]);
            }
            form.add(name, this.tr("Name"), null, "name");

            var value = new qx.ui.form.TextField().set({required: true});
            if (spec["value"]) {
                value.setValue(spec["value"]);
            }
            form.add(value, this.tr("Value"), null, "value");
            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            return {
                name: items["name"].getValue(),
                value: items["value"].getValue(),
                time: items["time"].getValue(),
                units: items["units"].getModelSelection().getItem(0)
            }
        }
    }
});