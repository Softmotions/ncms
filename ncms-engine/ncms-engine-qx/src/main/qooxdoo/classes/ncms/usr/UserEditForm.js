/**
 * Panel for editing user general information
 */
qx.Class.define("ncms.usr.UserEditForm", {
    extend : qx.ui.container.Composite,

    events : {
        /**
         * Data: updated user data.
         */
        "userUpdated" : "qx.event.type.Data"
    },

    construct : function() {
        this.base(arguments);
        this.setLayout(new qx.ui.layout.VBox(5));

        var form = this.__form = new qx.ui.form.Form();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        var el = new qx.ui.form.TextField();
        el.setRequired(true);
        el.tabFocus();
        form.add(el, this.tr("Name"), null, "name");

        el = new qx.ui.form.TextField();
        el.setRequired(true);
        form.add(el, this.tr("Full name"), null, "fullName");

        el = new qx.ui.form.TextField();
        el.setRequired(true);
        form.add(el, this.tr("E-Mail"), qx.util.Validate.email(), "email");

        el = new qx.ui.form.PasswordField();
        form.add(el, this.tr("Password"), null, "password");

        var fr = new sm.ui.form.FlexFormRenderer(form);
        fr._getLayout().setRowFlex(fr._row - 1, 1);
        this.add(fr, {flex : 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = new qx.ui.form.Button(this.tr("Save"));
        bt.addListener("execute", this.__save, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.__cancel, this);
        hcont.add(bt);

        this.add(hcont);
    },

    members : {
        __form : null,

        __user : null,

        setUser : function(name) {
            this.__user = name;
            var fitems = this.__form.getItems();

            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user", {name : this.__user}), "GET", "application/json");
            req.send(function(resp) {
                var data = resp.getContent();
                fitems["name"].setEnabled(false);
                fitems["name"].setValue(data["name"]);
                fitems["fullName"].setValue(data["fullName"]);
                fitems["email"].setValue(data["email"]);
                fitems["password"].setRequired(false);
                fitems["password"].setValue(null);
            }, this);
        },

        __save : function() {
            if (!this.__form.validate()) {
                return;
            }

            var fitems = this.__form.getItems();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user", {name : fitems["name"].getValue()}), "POST", "application/json");
            req.setParameter("email", fitems["email"].getValue());
            req.setParameter("fullname", fitems["fullName"].getValue());
            if (fitems["password"].getValue() != null && qx.lang.String.trimLeft(fitems["password"].getValue()) != "") {
                req.setParameter("password", fitems["password"].getValue());
            }

            req.send(function(resp) {
                this.fireDataEvent("userUpdated", resp.getContent());
                this.setUser(this.__user);
            }, this);
        },

        __cancel : function() {
            this.setUser(this.__user);
        }
    },

    destruct : function() {
        this.__user = null;
    }
});
