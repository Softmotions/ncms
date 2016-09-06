/**
 * Rename folder dialog
 */
qx.Class.define("ncms.mmgr.MediaItemRenameDlg", {
    extend: sm.ui.form.BaseSavePopupDlg,

    statics: {},

    events: {},

    properties: {},

    construct: function (path, name) {
        this.base(arguments);
        qx.core.Assert.assertArray(path);
        qx.core.Assert.assertString(name);
        this._path = path;
        var tf = this._form.getItems()["name"];
        tf.setValue(name);
        tf.selectAllText();
    },

    members: {

        _path: null,

        _configureForm: function () {
            var page = new qx.ui.form.TextField().set({allowGrowY: true, maxLength: 64, required: true});
            page.addListener("keypress", function (ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            this._form.add(page, this.tr("Folder"), null, "name");
            page.focus();
        },

        _save: function (cb) {
            var fitems = this._form.getItems();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move", this._path),
                "PUT", "application/json");
            req.addListenerOnce("finished", cb);
            var nname = fitems["name"].getValue();
            var npath = this._path.slice(0, -1).concat(nname).join("/");
            req.setData(npath);
            req.send(function (resp) {
                this.fireDataEvent("completed", [nname, npath]);
            }, this);
        }
    },

    destruct: function () {
        //this._disposeObjects("__field_name");
    }
});