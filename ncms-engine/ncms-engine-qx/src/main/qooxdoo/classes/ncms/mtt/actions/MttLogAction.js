/**
 * Mtt log message action
 */
qx.Class.define("ncms.mtt.actions.MttLogAction", {
    extend: qx.core.Object,
    implement: [ncms.mtt.actions.IMttAction],
    include: [qx.locale.MTranslation],


    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Write a log message");
        },

        getType: function () {
            return "log";
        },

        specForHuman: function (spec) {
            return (spec["level"] || "") + ": " + (spec["msg"] || "");
        }
    },

    members: {

        createWidget: function (spec, ruleId, actionId) {
            var form = new qx.ui.form.Form();
            var level = new qx.ui.form.SelectBox();
            ["INFO", "WARNING", "ERROR", "DEBUG"].forEach(function (l) {
                level.add(new qx.ui.form.ListItem(l, null, l));
            });
            if (spec["level"]) {
                level.setModelSelection([spec["level"]]);
            }
            form.add(level, this.tr("Log level"), null, "level");

            var msg = new qx.ui.form.TextArea().set({maxLength: 256});
            if (spec["msg"]) {
                msg.setValue(spec["msg"]);
            }
            form.add(msg, this.tr("Log message"), null, "msg", null, {flex:1});
            return new sm.ui.form.OneColumnFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            return {
                level: items["level"].getModelSelection().getItem(0),
                msg: items["msg"].getValue()
            }
        }
    }
});