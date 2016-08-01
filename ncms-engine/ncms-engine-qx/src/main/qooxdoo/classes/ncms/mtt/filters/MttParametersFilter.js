/**
 * Request parameters filter.
 * (utm_source, utm_campaign)
 */
qx.Class.define("ncms.mtt.filters.MttParametersFilter", {
    extend: qx.core.Object,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Request parameters");
        },

        getType: function () {
            return "params";
        },

        specForHuman: function (spec) {
            var data = spec["data"] || "";
            return data.split("\n").join(", ");
        }
    },

    members: {

        createWidget: function (spec) {
            var w = new qx.ui.container.Composite(new qx.ui.layout.VBox(4));
            w.add(new qx.ui.basic.Label(this._getDescriptionLabel()).set({rich: true, font: "monospace"}));
            var form = new qx.ui.form.Form();
            var data = new qx.ui.form.TextArea().set({maxLength: 1024, required: true});
            form.add(data, this._getRulesLabel(), null, "data", null, {flex: 1});
            if (spec["data"]) {
                data.setValue(spec["data"]);
            }
            var fr = new sm.ui.form.OneColumnFormRenderer(form);
            w.add(fr, {flex: 1});
            w.setUserData("form", form);
            return w;
        },

        asSpec: function (w) {
            var form = w.getUserData("form");
            if (form == null || !form.validate()) {
                return null;
            }
            var items = form.getItems();
            return {
                data: items["data"].getValue()
            }
        },

        _getRulesLabel: function() {
            return this.tr("Parameters filtering rules")
        },

        _getDescriptionLabel: function() {
            return this.tr(
                "Filter rules on each line:%1 <b>?</b> at end of param name means this parameter is not required",
                "<pre>" +
                "  param_name   = glob_mask<br>" +
                "  param_name? != glob_mask<br>" +
                "</pre>");
        }
    },

    destruct: function () {

    }
});
