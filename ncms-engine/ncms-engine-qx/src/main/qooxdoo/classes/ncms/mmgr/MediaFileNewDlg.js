/**
 * New file dialog
 */
qx.Class.define("ncms.mmgr.MediaFileNewDlg", {
    extend: sm.ui.form.BaseSavePopupDlg,

    /**
     * @param parentPaths {Array} Array of parent path segments.
     */
    construct: function (parentPaths) {
        this.base(arguments);
        qx.core.Assert.assertArray(parentPaths);
        this.__parentPaths = parentPaths;
    },

    members: {

        __parentPaths: null,

        _configureForm: function () {
            var page = new qx.ui.form.TextField().set({allowGrowY: true, maxLength: 64, required: true});
            page.addListener("keypress", function (ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            this._form.add(page, this.tr("File name"), null, "name");
            page.focus();
        },

        _save: function (cb) {
            var fitems = this._form.getItems();
            var name = fitems["name"].getValue();
            var url = ncms.Application.ACT.getRestUrl("media.upload");
            url = url + "/" +
                this.__parentPaths.concat(name)
                .filter(function (el) {
                    return encodeURIComponent(el)
                })
                .join("/");
            var req = new sm.io.Request(url, "PUT", "application/json");
            req.addListenerOnce("finished", cb);
            req.setData("");
            req.send(function (resp) {
                this.fireDataEvent("completed", resp.getContent());
            }, this);
        }
    },

    destruct: function () {
        this.__parentPaths = null;
    }
});
