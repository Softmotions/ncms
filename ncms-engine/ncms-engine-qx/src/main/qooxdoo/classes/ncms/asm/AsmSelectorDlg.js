/**
 * Assembly selector dialog window.
 */
qx.Class.define("ncms.asm.AsmSelectorDlg", {
    extend: qx.ui.window.Window,

    statics: {},

    events: {
        /**
         * Data: [] an array of selected asms.
         * @see ncms.asm.AsmSelector
         */
        "completed": "qx.event.type.Data"
    },

    properties: {},

    construct: function (caption, icon, constViewSpec, useColumns) {
        this.base(arguments, caption != null ? caption : this.tr("Select assembly"), icon);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });

        var selector = this.__selector =
            new ncms.asm.AsmSelector(
                constViewSpec,
                new sm.table.selection.ExtendedSelectionModel()
                .set({selectionMode: qx.ui.table.selection.Model.SINGLE_SELECTION}),
                useColumns);
        selector.getTable().addListener("cellDbltap", this.__ok, this);

        this.add(selector, {flex: 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);

        cmd = this.createCommand("Enter");
        cmd.addListener("execute", this.__ok, this);

        this.addListenerOnce("resize", this.center, this);
        selector.addListener("asmSelected", this.__syncState, this);

        this.__syncState();
    },

    members: {

        __saveBt: null,

        __selector: null,

        __ok: function () {
            if (!this.__saveBt.getEnabled()) {
                return;
            }
            var asms = this.__selector.getSelectedAsms();
            if (asms == null || asms.length === 0) {
                return;
            }
            this.fireDataEvent("completed", asms)
        },

        __syncState: function () {
            var asms = this.__selector.getSelectedAsms();
            this.__saveBt.setEnabled(asms.length > 0);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }

    },

    destruct: function () {
        this.__selector = null;
        this.__saveBt = null;
    }
});