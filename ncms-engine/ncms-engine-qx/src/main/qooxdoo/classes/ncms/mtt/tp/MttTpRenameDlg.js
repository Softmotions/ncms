/**
 * Rename `mtt tracking pixel` popup dialog.
 */
qx.Class.define("ncms.mtt.tp.MttTpRenameDlg", {
    extend: ncms.mtt.tp.MttTpNewDlg,

    construct: function (tpId, tpName) {
        this.base(arguments);
        this.__tpId = tpId;
        var f = this._form.getItems()["name"];
        f.setValue(tpName);
        f.tabFocus();
    },

    members: {

        __tpId: null,

        _save: function (cb) {
            var fitems = this._form.getItems();
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.tp.rename",
                    {
                        id: this.__tpId,
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