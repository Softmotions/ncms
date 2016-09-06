/**
 * New action group dialog.
 */
qx.Class.define("ncms.mtt.actions.MttActionGroupDlg", {
    extend: sm.ui.form.BaseSavePopupDlg,

    construct: function (ruleId, item) {
        qx.core.Assert.assertNumber(ruleId);
        this.__item = item;
        this.__ruleId = ruleId;
        this.base(arguments);
    },

    members: {

        __item: null,

        __ruleId: null,

        _configureForm: function () {
            var name = new qx.ui.form.TextField().set({allowGrowY: true, maxLength: 64, required: true});
            name.addListener("keypress", function (ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            this._form.add(name, this.tr("Group"), null, "name");
            if (this.__item != null) {
                name.setValue(this.__item.getLabel());
            }
            name.focus();
        },

        _save: function (cb) {
            var items = this._form.getItems();
            var req;
            if (this.__item == null) {
                req = new sm.io.Request(
                    //rs/adm/mtt/rules/rule/{id}/group/{name}
                    ncms.Application.ACT.getRestUrl("mtt.action.group.new",
                        {
                            id: this.__ruleId
                        }), "PUT", "application/json");
            } else {
                req = new sm.io.Request(
                    ///rs/adm/mtt/rules/group/{id}/{name}
                    ncms.Application.ACT.getRestUrl("mtt.action.group.update",
                        {
                            id: this.__item.getId(),
                            name: items["name"].getValue()
                        }), "POST", "application/json");
            }
            req.addListenerOnce("finished", cb);
            req.send(function (resp) {
                this.fireDataEvent("completed", resp.getContent());
            }, this);
        }
    },

    destruct: function () {
        this.__item = null;
        this.__ruleId = null;
    }
});