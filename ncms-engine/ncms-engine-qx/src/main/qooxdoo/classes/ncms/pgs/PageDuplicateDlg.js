/**
 * Duplicate page dialog.
 */
qx.Class.define("ncms.pgs.PageDuplicateDlg", {
    extend: ncms.pgs.PageChangeOrRenameDlg,

    construct: function (spec) {
        this.base(arguments, spec);
    },

    members: {

        _save: function (cb) {
            var items = this._form.getItems();
            var data = {
                id: this._spec["id"],
                name: items["name"].getValue(),
                type: items["container"].getValue() ? "page.folder" : "page"
            };
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.clone"), "PUT");
            req.addListenerOnce("finished", cb);
            req.setRequestContentType("application/json");
            req.setData(JSON.stringify(data));
            req.send(function (resp) {
                this.fireDataEvent("completed", data);
            }, this);
        }
    }
});