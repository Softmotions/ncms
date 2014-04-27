/**
 * Assembly selector dialog window.
 */
qx.Class.define("ncms.asm.AsmSelectorDlg", {
    extend : qx.ui.window.Window,

    statics : {
    },

    events : {
        /**
         * Data: [] array of selected asms.
         */
        "completed" : "qx.event.type.Data"
    },

    properties : {
    },

    construct : function(caption, icon, constViewSpec) {
        this.base(arguments, caption != null ? caption : this.tr("Select assembly"), icon);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 620,
            height : 400
        });

        var selector = this.__selector =
                new ncms.asm.AsmSelector(
                        constViewSpec,
                        new qx.ui.table.selection.Model()
                                .set({selectionMode : qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION}));
        selector.getTable().addListener("cellDblclick", this.__ok, this);

        this._add(selector, {flex : 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this._add(hcont);

        this.__closeCmd = new qx.ui.core.Command("Esc");
        this.__closeCmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);

        selector.addListener("asmSelected", this.__syncState, this);
        this.__syncState();
    },

    members : {

        __saveBt : null,

        __selector : null,

        __closeCmd : null,

        __dispose : function() {
            if (this.__closeCmd) {
                this.__closeCmd.setEnabled(false);
            }
            this._disposeObjects("__closeCmd");
            this.__selector = null;
            this.__closeCmd = null;
        },

        __ok : function() {
            this.fireDataEvent("completed", this.__selector.getSelectedAsms())
        },

        __syncState : function() {
            var asms = this.__selector.getSelectedAsms();
            this.__saveBt.setEnabled(asms.length > 0);
        },

        close : function() {
            this.base(arguments);
            this.__dispose();
        }

    },

    destruct : function() {
        this.__dispose();

    }
});