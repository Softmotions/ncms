qx.Class.define("nsu.legacy.ImportLegacyDataDlg", {
    extend : sm.ui.form.BasePopupDlg,

    construct : function(id) {
        this.base(arguments);
        this.__id = id;
        //todo remove it
        this._saveBt.setEnabled(false);
    },

    members : {

        __id : null,

        _configureForm : function() {
            var page = new qx.ui.form.TextField().set({allowGrowY : true, maxLength : 255, required : true});
            page.setPlaceholder(this.tr("Page address: http://nsu.ru/exp/...."));
            page.addListener("keypress", function(ev) {
                if (ev.getKeyIdentifier() == "Enter") {
                    this.save();
                }
            }, this);
            this._form.add(page, this.tr("URL"), null, "url");
            page.focus();
        },

        _save : function() {
            var data = this._form.populateJSONObject({});
            qx.log.Logger.info("DATA=" + JSON.stringify(data));

            /*var url = ncms.Application.ACT.getRestUrl("media.folder.put", path);
             var req = new sm.io.Request(url, "PUT", "application/json");
             req.send(function(resp) {
             this.fireDataEvent("completed", resp.getContent());
             }, this);*/
        }
    },

    destruct : function() {
        this.__id = null;
    }
});