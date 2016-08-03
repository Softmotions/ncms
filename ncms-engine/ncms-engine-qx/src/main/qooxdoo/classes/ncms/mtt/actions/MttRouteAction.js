/**
 * Mtt route
 * @asset (ncms/icon/16/mtt/route.png)
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
            return spec["target"] || "";
        }
    },

    members: {

        createWidget: function (spec, ruleId, actionId) {
            var form = new qx.ui.form.Form();

            var bf = new sm.ui.form.ButtonField(null, "ncms/icon/16/mtt/route.png");
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
                    allowExternalLinks: true,
                    requireLinkName: false
                });
                dlg.addListenerOnce("completed", function (ev) {
                    var data = ev.getData();
                    var val = [];
                    if (!sm.lang.String.isEmpty(data["externalLink"])) {
                        val.push(data["externalLink"]);
                    } else {
                        val.push("page:" + sm.lang.Array.lastElement(data["guidPath"]));
                    }
                    if (!sm.lang.String.isEmpty(data["linkText"])) {
                        val.push(data["linkText"]);
                    }
                    bf.setValue(val.join(" | "));
                    dlg.close();
                }, this);
                dlg.open();
            }, this);
            form.add(bf, this.tr("Route"), null, "target");
            return new sm.ui.form.FlexFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            return {
                target: form.getItems()["target"].getValue()
            };
        }
    }
});