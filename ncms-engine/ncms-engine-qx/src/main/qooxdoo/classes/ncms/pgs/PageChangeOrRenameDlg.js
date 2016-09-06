/**
 * Change page name/type dialog.
 * Similar to new page popup dialog.
 */
qx.Class.define("ncms.pgs.PageChangeOrRenameDlg", {
    extend: ncms.pgs.PageNewDlg,

    construct: function (spec) {
        this._spec = spec;
        this.base(arguments);
    },

    members: {

        _spec: null,

        _configureForm: function () {
            this.base(arguments);
            var items = this._form.getItems();
            items["name"].setValue(this._spec["label"]);
            items["container"].setValue((this._spec["status"] & 1) != 0);
        },

        _save: function (cb) {
            var items = this._form.getItems();
            var data = {
                id: this._spec["id"],
                name: items["name"].getValue(),
                type: items["container"].getValue() ? "page.folder" : "page"
            };
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.update.basic"), "PUT");
            req.addListenerOnce("finished", cb);
            req.setRequestContentType("application/json");
            req.setData(JSON.stringify(data));
            req.send(function (resp) {
                this.fireDataEvent("completed", data);
            }, this);
        }

    },

    destruct: function () {
        //this._disposeObjects("__field_name");
    }
});