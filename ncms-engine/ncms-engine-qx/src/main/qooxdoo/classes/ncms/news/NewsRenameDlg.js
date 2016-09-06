qx.Class.define("ncms.news.NewsRenameDlg", {
    extend: ncms.news.NewsNewDlg,

    construct: function (id, name) {
        this._name = name;
        this.base(arguments, id);
        this._form.getItems()["name"].setValue(name);

    },

    members: {

        _name: null,

        _save: function (cb) {
            var items = this._form.getItems();
            var data = {
                id: this._id,
                name: items["name"].getValue(),
                type: "news.page"
            };
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.update.basic"), "PUT");
            req.addListenerOnce("finished", cb);
            req.setRequestContentType("application/json");
            req.setData(JSON.stringify(data));
            req.send(function (resp) {
                this.fireDataEvent("completed", data);
            }, this);
        },

        destruct: function () {
            this._name = null;
        }
    }
});