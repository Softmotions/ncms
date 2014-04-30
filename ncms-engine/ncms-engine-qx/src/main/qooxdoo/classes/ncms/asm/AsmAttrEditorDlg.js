/**
 * Attribute editor dialog
 */
qx.Class.define("ncms.asm.AsmAttrEditorDlg", {
    extend : qx.ui.window.Window,

    statics : {
    },

    events : {
        /**
         * Data: assembly attribute JSON representation.
         */
        "completed" : "qx.event.type.Data"
    },

    properties : {
    },

    /**
     * attrSpec example:
     * {
     *  "asmId" : 1,
     *  "name" : "copyright",
     *  "type" : "string",
     *  "value" : "My company (c)",
     *  "options" : null,
     *   "hasLargeValue" : false
     * }
     *
     */
    construct : function(caption, icon, attrSpec) {
        this.base(arguments, caption != null ? caption : this.tr("Edit assembly attribute"), icon);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 620,
            height : 400
        });

        var aspec = this.__attrSpec = attrSpec;

        var form = this.__form = new qx.ui.form.Form();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        //attribute name
        var el = new qx.ui.form.TextField();
        el.setRequired(true);
        el.tabFocus();
        form.add(el, this.tr("Name"), null, "name");

        el = new qx.ui.form.TextField();
        form.add(el, this.tr("Options"), null, "options");

        el = new qx.ui.form.SelectBox();
        el.add(new qx.ui.form.ListItem(this.tr("String"), null, "string"));
        el.add(new qx.ui.form.ListItem(this.tr("Assembly reference"), null, "asmref"));
        el.add(new qx.ui.form.ListItem(this.tr("Resource"), null, "resource"));
        form.add(el, this.tr("Type"), null, "type");

        el = new qx.ui.form.TextArea();
        form.add(el, this.tr("Value"), null, "value");

        var fr = new sm.ui.form.FlexFormRenderer(form);
        fr._getLayout().setRowFlex(fr._row - 1, 1);
        this.add(fr, {flex : 1});


        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        this.__closeCmd = new qx.ui.core.Command("Esc");
        this.__closeCmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members : {

        __form : null,

        __closeCmd : null,

        __attrSpec : null,

        __dispose : function() {
            if (this.__closeCmd) {
                this.__closeCmd.setEnabled(false);
            }
            this._disposeObjects("__form", "__closeCmd");
        },

        __ok : function() {
            if (!this.__form.validate()) {
                return;
            }
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