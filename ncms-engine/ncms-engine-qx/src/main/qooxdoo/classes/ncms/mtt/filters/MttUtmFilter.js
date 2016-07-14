/**
 * Utm marks filter. (utm_source, utm_campaign)
 */
qx.Class.define("ncms.mtt.filters.MttUtmFilter", {
    extend: qx.core.Object,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Utm marks filter");
        },

        getType: function () {
            return "utm";
        },

        specForHuman: function (spec) {
            return "todo";
        }
    },

    members: {

        createWidget: function (spec) {
            var w = new qx.ui.core.Widget();
            w.setBackgroundColor("blue");
            return w;
        },

        asSpec: function (w) {
            return {};
        }
    },

    destruct: function () {

    }
});
