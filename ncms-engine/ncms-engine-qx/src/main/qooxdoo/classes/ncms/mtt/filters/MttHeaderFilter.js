/**
 * Http header filter
 */
qx.Class.define("ncms.mtt.filters.MttHeaderFilter", {
    extend: qx.core.Object,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("HTTP header filter");
        },

        getType: function () {
            return "header";
        },

        specForHuman: function (spec) {
            return "todo";
        }
    },

    members: {

        createWidget: function (spec) {
            var w = new qx.ui.core.Widget();
            w.setBackgroundColor("red");
            return w;
        },

        asSpec: function (w) {
            return {};
        }

    },

    destruct: function () {

    }
});