/**
 * Mtt route
 */
qx.Class.define("ncms.mtt.actions.MttRouteAction", {
    extend: qx.core.Object,
    implement: [ncms.mtt.actions.IMttAction],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Route to another resource");
        },

        getType: function () {
            return "route";
        },

        specForHuman: function (spec) {
            return "todo";
        }
    },

    members: {

        createWidget: function (spec) {
            var w = new qx.ui.core.Widget();
            w.setBackgroundColor("gray");
            return w;
        },

        asSpec: function (w) {
            return {};
        }
    }
});