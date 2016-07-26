/**
 * Mtt route
 *
 * @asset (ncms/icon/16/misc/chain.png)
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
            var me = this;
            var form = new qx.ui.form.Form();

            var bf = new sm.ui.form.ButtonField(this.tr("Route"), "ncms/icon/16/misc/chain.png");
            bf.setShowResetButton(true);
            bf.setReadOnly(false);
            bf.setRequired(true);
            bf.setPlaceholder(this.tr("Please set the route destination"));
            bf.addListener("reset", function () {
                bf.resetValue();
            });
            if (spec["target"] != null) {
                bf.setValue(spec["target"]);
            }
            bf.addListener("execute", function () {
                var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Please set the route destination"), {
                    allowExternalLinks: true
                });
                dlg.addListenerOnce("completed", function (ev) {
                    var data = ev.getData();
                    console.log('DATA=' + JSON.stringify(data));
                }, this);
                dlg.open();
            }, this);
            form.add(bf, this.tr("Route"), null, "route");

            var fr = new sm.ui.form.FlexFormRenderer(form);
            return fr;
        },

        asSpec: function (w) {
            return {};
        }
    }
});