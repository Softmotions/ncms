/**
 * Mtt set request parameters action
 */
qx.Class.define("ncms.mtt.actions.MttSetRequestParametersAction", {
    extend: qx.core.Object,
    implement: [ncms.mtt.actions.IMttAction],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Set a request parameters");
        },

        getType: function () {
            return "parameters";
        },

        specForHuman: function (spec) {
            return "overwrite:" + spec["overwrite"] + " params:" + spec["params"];
        }
    },

    members: {

        createWidget: function (spec, ruleId, actionId) {
            var form = new qx.ui.form.Form();

            var overwrite = new qx.ui.form.CheckBox(this.tr("Overwrite"));
            if (spec["overwrite"] != null) {
                overwrite.setValue(!!spec["overwrite"]);
            } else {
                overwrite.setValue(true);
            }
            form.add(overwrite, "", null, "overwrite");

            var params = new qx.ui.form.TextField().set({required: true});
            params.setPlaceholder(this.tr("param_name1=value1, param_name2=value"));
            if (spec["params"]) {
                params.setValue(spec["params"]);
            }
            form.add(params, this.tr("Request parameters"), null, "params");
            return new sm.ui.form.OneColumnFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            return {
                overwrite: items["overwrite"].getValue(),
                params: items["params"].getValue()
            }
        }
    }
});