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

        var cont = new qx.ui.container.Composite(new qx.ui.layout.VBox(5, "top"));

        // Header
        this.__header = new qx.ui.basic.Label().set({font: "headline"});
        cont.add(this.__header);

        // Form
        var form = this.__form = new sm.ui.form.ExtendedForm();

        var el = new qx.ui.form.CheckBox(this.tr("Enabled"));
        el.addListener("changeValue", this.__onModified, this);
        form.add(el, "", null, "enabled");

        el = new qx.ui.form.TextField().set({maxLength: 64});
        el.setRequired(true);
        el.setPlaceholder(this.tr("utm_source=glob_mask, other_parameter=glob_mask"));
        el.addListener("input", this.__onModified, this);
        form.add(el, this.tr("Incoming request parameters"), null, "params");

        el = new qx.ui.form.TextField().set({maxLength: 128});
        el.setPlaceholder(this.tr("http://....?client_id={client_id}"));
        el.addListener("input", this.__onModified, this);
        form.add(el, this.tr("Tracking pixel URL template"), null, "url");

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

        },

        __cancel: function () {
            var val = qx.lang.Object.mergeWith({}, this.getTp() || {});
            this.setTp(val);
        },

        __onModified: function () {
            this.__broadcaster.setEnabled(true);
        },

        __applyTp: function (val, old) {
            console.log("Apply tp=" + JSON.stringify(val));
            var items = this.__form.getItems();
            if (val && val.id) {
                this.__header.setValue(val["name"]);
                items["enabled"].setValue(!!val["enabled"]);
                var req = ncms.Application.request("mtt.tp.get", {id: val.id});
                req.send(function (resp) {
                    // {
                    //  "id":21,"name":"1facebook.com","description":null,
                    //  "cdate":1472137787318,"mdate":1472211396484,"enabled":true,
                    //  "spec":"{}"
                    // }
                    var data = resp.getContent();
                    var spec = JSON.parse(data["spec"]);
                    items["enabled"].setValue(!!data["enabled"]);
                    this.__header.setValue(data["name"]);
                    items["jscode"].setValue(spec["jscode"] || "");
                    items["url"].setValue(spec["url"] || "");
                    this.__broadcaster.setEnabled(false);
                }, this);
            } else {
                this.__header.resetValue();
                items["enabled"].resetValue();
                items["jscode"].resetValue();
                items["url"].resetValue();
                this.__broadcaster.setEnabled(false);
            }
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