/**
 * Mtt set A/B testing marks.
 */
qx.Class.define("ncms.mtt.actions.MttABMarksAction", {
    extend: qx.core.Object,
    implement: [ncms.mtt.actions.IMttAction],
    include: [qx.locale.MTranslation],


    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Set A/B marks");
        },

        getType: function () {
            return "abmarks";
        },

        specForHuman: function (spec) {
            return spec["marks"] + " | " + spec["units"] + "=" + spec["time"];
        }
    },

    members: {

        createWidget: function (spec, ruleId, actionId) {
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
            name.setPlaceholder(this.tr("Comma separated A/B marks: A,B,C..."));
            if (spec["marks"]) {
                name.setValue(spec["marks"]);
            }
            form.add(name, this.tr("A/B Marks"), null, "marks", null, {fullRow: true, flex: 1});
            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            return {
                marks: items["marks"].getValue(),
                time: items["time"].getValue(),
                units: items["units"].getModelSelection().getItem(0)
            }
        }
    }
});