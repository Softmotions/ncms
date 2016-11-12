/**
 * Create new page popup dialog.
 */
qx.Class.define("ncms.pgs.PageNewDlg", {
    extend: sm.ui.form.BaseSavePopupDlg,

    construct: function (parentId) {
        this.base(arguments);
        this.set({width: 350});
        this._id = parentId;
    },

    members: {

        _id: null,

        _configureForm: function () {
            var el = new qx.ui.form.TextField().set({allowGrowY: true, maxLength: 128, required: true});
            el.addListener("keypress", function (ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            el.setToolTipText(this.tr("Page name"));
            this._form.add(el, this.tr("Page name"), null, "name");
            el.focus();

            el = new qx.ui.form.CheckBox();
            el.setToolTipText(this.tr("Page is container folder for other pages"));
            this._form.add(el, this.tr("Container"), null, "container");
        },

        _save: function (cb) {
            var items = this._form.getItems();
            var data = {
                name: items["name"].getValue(),
                parent: this._id,
                type: items["container"].getValue() ? "page.folder" : "page"
            };
            var req = new sm.io.Request(ncms.Application.ACT.getUrl("pages.new"), "PUT", "application/json");
            req.addListenerOnce("finished", cb);
            req.setRequestContentType("application/json");
            req.setData(JSON.stringify(data));
            req.send(function (resp) {
                this.fireDataEvent("completed", resp.getContent());
            }, this);
        }
    },

    destruct: function () {
        this._id = null;
    }
});