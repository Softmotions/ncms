/**
 * Remember traffic sources action
 *
 */
qx.Class.define("ncms.mtt.actions.MttRememberOriginAction", {
    extend: qx.core.Object,
    implement: [ncms.mtt.actions.IMttAction],
    include: [qx.locale.MTranslation],


    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Track traffic sources");
        },

        getType: function () {
            return "remember";
        },

        specForHuman: function (spec) {
            var ret = [];
            if (spec["tp"]) {
                ret.push(qx.locale.Manager.tr("Tracking pixels"));
            }
            return ret.join(", ");
        }
    },

    members: {

        createWidget: function (spec, ruleId, actionId) {
            var w = new qx.ui.container.Composite(new qx.ui.layout.Grid(10, 10)).set({
                paddingTop: 10
            });

            var tpcb = new qx.ui.form.CheckBox(this.tr("Enable tracking pixels"));
            tpcb.setValue(!!spec["tp"]);
            w.add(tpcb, {row: 0, column: 0});
            w._tpcb = tpcb;

            /*var tpbt = new qx.ui.form.Button(this.tr("Manage tracking pixels"),
                "ncms/icon/16/misc/color-swatch.png");
            tpbt.addListener("execute", function() {
                // todo
            });
            w.add(tpbt, {row: 0, column: 1});

            tpcb.bind("value", tpbt, "enabled"); */
            return w;
        },

        asSpec: function (w) {
            var tpcb = w._tpcb;
            if (!tpcb) return {};
            return {
                "tp": !!tpcb.getValue()
            }
        }
    }
});
