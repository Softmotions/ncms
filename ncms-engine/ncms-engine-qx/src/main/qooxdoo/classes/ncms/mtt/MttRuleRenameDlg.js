/**
 * 'Rename assembly' popup dialog.
 */
qx.Class.define("ncms.mtt.MttRuleRenameDlg", {
    extend: ncms.mtt.MttRuleNewDlg,

    construct: function (ruleId, ruleName) {
        this.base(arguments);
        this.__ruleId = ruleId;
        var f = this._form.getItems()["name"];
        f.setValue(ruleName);
        f.tabFocus();
    },

    members: {

        __ruleId: null,

        _save: function (cb) {
            var fitems = this._form.getItems();
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.rules.rename",
                    {
                        id: this.__ruleId,
                        name: fitems["name"].getValue()
                    }
                ),
                "PUT", "application/json");
            req.addListenerOnce("finished", cb);
            req.send(function (resp) {
                this.fireDataEvent("completed", resp.getContent());
            }, this);
        }
    }
});