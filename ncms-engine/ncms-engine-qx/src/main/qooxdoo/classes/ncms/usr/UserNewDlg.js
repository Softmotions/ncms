/**
 * New user dialog
 */
qx.Class.define("ncms.usr.UserNewDlg", {
    extend : qx.ui.window.Window,

    statics : {
    },

    events : {
        /**
         * Data: created user.
         */
        "completed" : "qx.event.type.Data"
    },

    properties : {
    },

    construct : function(caption, icon, constViewSpec) {
        this.base(arguments, caption != null ? caption : this.tr("New user"), icon);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 540,
            height : 150
        });

        var form = this.__form = new qx.ui.form.Form();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        var el = new qx.ui.form.TextField();
        el.setRequired(true);
        el.tabFocus();
        form.add(el, this.tr("Name"), null, "name");

        el = new qx.ui.form.TextField();
        el.setRequired(true);
        form.add(el, this.tr("Full name"), null, "fullname");

        el = new qx.ui.form.TextField();
        el.setRequired(true);
        form.add(el, this.tr("E-Mail"), qx.util.Validate.email(), "email");

        el = new qx.ui.form.PasswordField();
        el.setRequired(true);
        form.add(el, this.tr("Password"), null, "password");

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

        __dispose : function() {
            if (this.__closeCmd) {
                this.__closeCmd.setEnabled(false);
            }
            this._disposeObjects("__closeCmd");
            this.__closeCmd = null;
        },

        __ok : function(ev) {
            if (!this.__form.validate()) {
                return;
            }

            var fitems = this.__form.getItems();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user", {name : fitems["name"].getValue()}), "POST", "application/json");
            req.setParameter("email", fitems["email"].getValue());
            req.setParameter("fullname", fitems["fullname"].getValue());
            req.setParameter("password", fitems["password"].getValue());

            req.send(function(resp) {
                this.fireDataEvent("completed", resp.getContent());
            }, this);
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
