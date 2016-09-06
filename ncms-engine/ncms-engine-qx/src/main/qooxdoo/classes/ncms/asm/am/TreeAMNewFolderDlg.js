/**
 * New folder dialog
 */

qx.Class.define("ncms.asm.am.TreeAMNewFolderDlg", {
    extend: sm.ui.form.BaseSavePopupDlg,

    construct: function () {
        this.base(arguments);
    },

    members: {

        _configureForm: function () {
            var el = new qx.ui.form.TextField().set({allowGrowY: true, maxLength: 128, required: true});
            el.addListener("keypress", function (ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            el.setToolTipText(this.tr("Folder name"));
            this._form.add(el, this.tr("Name"), null, "name");
            el.focus();
        },

        _save: function (cb) {
            var items = this._form.getItems();
            var data = items["name"].getValue();
            this.fireDataEvent("completed", data);
            cb();
        }
    },

    destruct: function () {
        //this._disposeObjects("__field_name");                                
    }
});