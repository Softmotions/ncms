/**
 * Options dialog for {@link ncms.asm.am.RichResourceRefAMC}
 */
qx.Class.define("ncms.asm.am.RichResourceRefAMCOptsDlg", {
    extend : qx.ui.window.Window,

    events : {
        "completed" : "qx.event.type.Data"
    },


    construct : function(attrSpec, asmSpec, options) {
        this.__options = options || {};
        this.__attrSpec = attrSpec;
        this.__asmSpec = asmSpec;

        this.base(arguments, this.tr("Rich resource reference GUI options"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true/*,
             width : 620,
             height : 400*/
        });

        this.add(this.__configureForm(), {flex : 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        //hcont.setPadding(5);

        var bt = new qx.ui.form.Button(this.tr("Ok"));
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

        __options : null,

        __form : null,

        __attrSpec : null,

        __asmSpec : null,

        __configureForm : function() {
            var form = this.__form = new sm.ui.form.ExtendedForm();
            var opts = this.__options;

            var el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowDescription"] == "true");
            form.add(el, this.tr("Allow description"), null, "allowDescription");

            el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowImage"] == "true");
            form.add(el, this.tr("Allow image"), null, "allowImage");

            return new sm.ui.form.FlexFormRenderer(form);
        },

        __ok : function() {
            var data = {};
            //todo fill the data
            this.fireDataEvent("completed", data);
        }
    },

    destruct : function() {
        this.__options = null;
        this.__asmSpec = null;
        this.__attrSpec = null;
        this._disposeObjects("__form");
    }
});