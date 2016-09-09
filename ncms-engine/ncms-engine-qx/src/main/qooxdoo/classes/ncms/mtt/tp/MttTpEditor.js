/**
 * Tracking pixels editor side.
 */
qx.Class.define("ncms.mtt.tp.MttTpEditor", {
    extend: qx.ui.container.Scroll,

    properties: {

        /**
         * Set tracking pixel id.
         */
        "tp": {
            check: "Object",
            apply: "__applyTp",
            nullable: true
        }
    },

    construct: function () {
        this.base(arguments);


        var bk = this.__broadcaster = sm.event.Broadcaster.create({
            "enabled": false
        });

        var cont =
            new qx.ui.container.Composite(new qx.ui.layout.VBox(5, "top"))
            .set({padding: [0, 20, 0, 0]});

        // Header
        this.__header = new qx.ui.basic.Label().set({font: "headline"});
        cont.add(this.__header);

        // Form
        var form = this.__form = new sm.ui.form.ExtendedForm();

        var el = new qx.ui.form.CheckBox(this.tr("Enabled"));
        el.addListener("changeValue", this.__onModified, this);
        form.add(el, "", null, "enabled");

        // Patterns for incoming request parameters
        el = new qx.ui.form.TextField().set({maxLength: 64});
        el.setRequired(true);
        el.setPlaceholder(this.tr("utm_source=glob_mask, other_parameter=glob_mask"));
        el.addListener("input", this.__onModified, this);
        form.add(el, this.tr("Incoming request parameters patterns"), null, "params");

        // Transferred (saved) params
        el = new qx.ui.form.TextField().set({maxLength: 128});
        el.setRequired(false);
        el.setPlaceholder(this.tr("Comma separated list of request params to save"));
        el.addListener("input", this.__onModified, this);
        form.add(el, this.tr("Saved request params"), null, "tparams");

        el = new qx.ui.form.TextField().set({maxLength: 128});
        el.setPlaceholder(this.tr("http://....?client_id={client_id}"));
        el.addListener("input", this.__onModified, this);
        form.add(el, this.tr("Tracking pixel URL template"), null, "url");

        el = new qx.ui.form.TextArea().set({maxLength: 256, minimalLineHeight: 2});
        el.addListener("input", this.__onModified, this);
        form.add(el, this.tr("Description"), null, "description");

        el = new qx.ui.form.TextArea().set({maxLength: 2048, autoSize: true, maxHeight: 400});
        el.addListener("input", this.__onModified, this);
        form.add(el, this.tr("Tracking pixel JS code"), null, "jscode");

        var fr = new sm.ui.form.OneColumnFormRenderer(form);
        cont.add(fr);

        // Buttons
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);
        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Save"));
        bt.addListener("execute", this.__ok, this);
        bk.attach(bt, "enabled", "enabled");
        hcont.add(bt);

        bt = this.__cancelBt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.__cancel, this);
        bk.attach(bt, "enabled", "enabled");
        hcont.add(bt);
        cont.add(hcont);


        this.add(cont);
    },

    members: {

        __broadcaster: null,

        __header: null,

        __form: null,


        __okBt: null,

        __cancelBt: null,

        __ok: function () {
            var tp  = this.getTp();
            if (tp == null) {
                return;
            }
            if (!this.__form.validate()) {
                return;
            }
            var data = this.__form.populateJSONObject();
            var req = ncms.Application.request("mtt.tp.update", {id: tp["id"]}, "POST");
            req.setData(JSON.stringify(data));
            req.send(function (resp) {
                this.__applyTpObj(resp.getContent());
            }, this)
        },

        __cancel: function () {
            var val = qx.lang.Object.mergeWith({}, this.getTp() || {});
            this.setTp(val);
        },

        __onModified: function () {
            this.__broadcaster.setEnabled(true);
        },

        __applyTp: function (val, old) {
            var items = this.__form.getItems();
            if (val && val.id) {
                this.__header.setValue(val["name"]);
                var req = ncms.Application.request("mtt.tp.get", {id: val.id});
                req.send(function (resp) {
                    this.__applyTpObj(resp.getContent());
                }, this);
            } else {
                this.__applyTpObj();
            }
        },

        __applyTpObj: function (data) {
            var items = this.__form.getItems();
            if (data) {
                var spec = JSON.parse(data["spec"]);
                this.__header.setValue(data["name"]);
                items["description"].setValue(data["description"] || "");
                items["enabled"].setValue(!!data["enabled"]);
                items["jscode"].setValue(spec["jscode"] || "");
                items["url"].setValue(spec["url"] || "");
                items["params"].setValue(spec["params"] || "");
                items["tparams"].setValue(spec["tparams"] || "");
            } else {
                this.__header.resetValue();
                items["enabled"].resetValue();
                items["jscode"].resetValue();
                items["description"].resetValue();
                items["url"].resetValue();
                items["params"].resetValue();
                items["tparams"].resetValue();
            }
            this.__broadcaster.setEnabled(false);
        }
    },

    destruct: function () {
        this.__header = null;
        this.__form = null;
        this.__okBt = null;
        this.__cancelBt = null;
        if (this.__broadcaster) {
            this.__broadcaster.destruct();
            this.__broadcaster = null;
        }
    }
});