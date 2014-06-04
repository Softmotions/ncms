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

    construct : function(parentId) {
        this.base(arguments);
        this.__parentId = parentId;
    },

    members : {

        __parentId : null,

        _configureForm : function() {
            var el = new qx.ui.form.TextField().set({allowGrowY : true, maxLength : 64, required : true});
            el.addListener("keypress", function(ev) {
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