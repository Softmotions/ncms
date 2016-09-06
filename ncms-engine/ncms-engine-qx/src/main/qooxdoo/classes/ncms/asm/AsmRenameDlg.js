/**
 * 'Rename assembly' popup dialog.
 */
qx.Class.define("ncms.asm.AsmRenameDlg", {
    extend: ncms.asm.AsmNewDlg,

    construct: function (asmId, asmName) {
        this.base(arguments);
        this.__asmId = asmId;
        var f = this._form.getItems()["name"];
        f.setValue(asmName);
        f.tabFocus();
    },

    members: {
        __asmId: null,

        _save: function (cb) {
            var fitems = this._form.getItems();
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("asms.rename",
                    {
                        id: this.__asmId,
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