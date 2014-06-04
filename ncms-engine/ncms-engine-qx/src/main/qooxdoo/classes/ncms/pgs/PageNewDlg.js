/**
 * Create new page popup dialog.
 */
qx.Class.define("ncms.pgs.PageNewDlg", {
    extend : sm.ui.form.BaseSavePopupDlg,

    statics : {
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
    },

    members : {

        _configureForm : function() {
            var name = new qx.ui.form.TextField().set({allowGrowY : true, maxLength : 64, required : true});
            name.addListener("keypress", function(ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            this._form.add(name, this.tr("Page name"), null, "name");
            name.focus();
        },

        _save : function() {
            var items = this._form.getItems();

            /*var req = new sm.io.Request(
             ncms.Application.ACT.getRestUrl("asms.new", {name : fitems["name"].getValue()}),
             "PUT", "application/json");
             req.send(function(resp) {
             this.fireDataEvent("completed", resp.getContent());
             }, this);*/
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});