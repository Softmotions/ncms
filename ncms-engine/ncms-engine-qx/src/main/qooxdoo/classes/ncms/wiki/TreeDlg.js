/**
 * Insert tree dialog.
 */
qx.Class.define("ncms.wiki.TreeDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * Data: {
         *   style : {String} simple|dynamic,
         *   open : {Boolean|null} Makes sens only for `dynamic` style
         * }
         */
        "completed": "qx.event.type.Data"
    },

    construct: function () {
        this.base(arguments, this.tr("Insert tree"));
        this.setLayout(new qx.ui.layout.VBox(4));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 350
        });

        var form = this.__form = new sm.ui.form.ExtendedForm();
        var rg = new qx.ui.form.RadioButtonGroup();
        rg.add(new qx.ui.form.RadioButton(this.tr("Simple tree")).set({"model": "simple"}));
        rg.add(new qx.ui.form.RadioButton(this.tr("Dynamic tree")).set({"model": "dynamic"}));
        form.add(rg, this.tr("Tree style"), null, "style");

        rg.addListener("changeSelection", function (ev) {
            var w = ev.getData()[0];
            cb.setEnabled(w.getModel() === "dynamic");
        });

        var cb = new qx.ui.form.CheckBox();
        cb.setEnabled(false);
        form.add(cb, this.tr("Open all nodes"), null, "open");

        var fr = new sm.ui.form.FlexFormRenderer(form);
        this.add(fr);

        //
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this._okBt = new qx.ui.form.Button(this.tr("Ok"));
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

        __form: null,

        __ok: function () {
            var data = {};
            this.__form.populateJSONObject(data);
            this.fireDataEvent("completed", data);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this._disposeObjects("__form");
    }
});