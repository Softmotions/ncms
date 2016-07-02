/**
 * Dialog wraps AM
 */
qx.Class.define("ncms.asm.am.AMWrapperDlg", {
    extend: qx.ui.window.Window,

    events: {
        completed: "qx.event.type.Data"
    },

    construct: function (amClass, attrSpec, asmSpec, opts) {
        opts = this.__opts = opts || {};
        if (["value", "options"].indexOf(opts["mode"]) === -1) {
            opts["mode"] = "options";
        }
        this.base(arguments, amClass.getDescription());
        this.setLayout(new qx.ui.layout.VBox());
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });

        var am = this.__am = new amClass();
        var w = (opts["mode"] === "value") ?
                am.activateValueEditorWidget(attrSpec, asmSpec) :
                am.activateOptionsWidget(attrSpec, asmSpec);

        this.add(w, {flex: 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Ok"));
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

    members: {

        __am: null,

        __opts: null,

        __okBt: null,

        __ok: function () {
            var data = (this.__opts["mode"] === "value") ? this.__am.valueAsJSON() : this.__am.optionsAsJSON();
            if (data == null) {
                return;
            }
            this.fireDataEvent("completed", data);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this.__okBt = null;
        this.__opts = null;
        this._disposeObjects("__am");
    }
});