qx.Class.define("ncms.news.NewsNewDlg", {
    extend : sm.ui.form.BaseSavePopupDlg,

    construct : function(parentId) {
        this.base(arguments);
        this.set({width : 350});
        this._id = parentId;
    },

    members : {

        _id : null,

        _configureForm : function() {
            var el = new qx.ui.form.TextField().set({allowGrowY : true, maxLength : 64, required : true});
            el.addListener("keypress", function(ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            el.setToolTipText(this.tr("News caption"));
            this._form.add(el, this.tr("News caption"), null, "name");
            el.focus();
        },

        _save : function() {
            var items = this._form.getItems();
            var data = {
                name : items["name"].getValue(),
                parent : this._id,
                type : "news.page"
            };
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.new"), "PUT");
            req.setRequestContentType("application/json");
            req.setData(JSON.stringify(data));
            req.send(function(resp) {
                this.fireDataEvent("completed", data);
            }, this);
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});