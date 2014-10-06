qx.Class.define("nsu.legacy.ImportLegacyDataDlg", {
    extend : qx.ui.window.Window,

    events : {
        "completed" : "qx.event.type.Data"
    },

    construct : function(id) {
        this.__id = id;
        this.base(arguments, this.tr("Importing page from old nsu.ru"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 550
        });

        var form = this.__form = new sm.ui.form.ExtendedForm();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        //name
        var el = new qx.ui.form.TextField();
        el.setRequired(true);
        el.setMaxLength(255);
        el.setPlaceholder(this.tr("Page address: http://nsu.ru/exp/...."));
        form.add(el, this.tr("URL"), null, "url");
        el.addListener("input", function(ev) {
            this.__okBt.setEnabled(!sm.lang.String.isEmpty(ev.getData()));
        }, this);

        var wikiCb = el = new qx.ui.form.CheckBox();
        form.add(el, this.tr("Import wiki"), null, "wiki");

        var fixLinks = el = new qx.ui.form.CheckBox();
        fixLinks.setEnabled(false);
        fixLinks.setValue(true);
        form.add(el, this.tr("Fix links"), null, "links");

        wikiCb.addListener("changeValue", function(ev) {
            fixLinks.setEnabled(ev.getData() == true);
        });

        var fr = new sm.ui.form.FlexFormRenderer(form);
        fr.setPaddingBottom(10);
        this.add(fr);

        //Footer
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Ok"));
        this.__okBt.setEnabled(false);
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members : {

        __id : null,

        __form : null,

        __okBt : null,

        __ok : function() {
            if (!this.__form.validate()) {
                return;
            }
            this.__form.getItems();
            var data = this.__form.populateJSONObject({});
            var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("nsu.legacy.import", {id : this.__id}),
                    "PUT", "application/json");
            req.setData(JSON.stringify(data));
            req.send(function(resp) {
                this.fireDataEvent("completed", resp.getContent());
            }, this);
        }
    },

    destruct : function() {
        this._disposeObjects("__form");
        this.__id = this.__okBt = null;
    }
});