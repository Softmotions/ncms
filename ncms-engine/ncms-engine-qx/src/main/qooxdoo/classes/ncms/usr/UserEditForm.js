/**
 * Panel for editing user general information
 */
qx.Class.define("ncms.usr.UserEditForm", {
    extend: qx.ui.container.Composite,

    events: {
        /**
         * Data: updated user data.
         */
        "userUpdated": "qx.event.type.Data"
    },

    construct: function (editable) {
        this.__broadcaster = sm.event.Broadcaster.create({
            modified: false
        });

        this.base(arguments);

        this.setLayout(new qx.ui.layout.VBox(5));

        this.__editable = editable === undefined ? true : !!editable;

        var form = this.__form = new qx.ui.form.Form();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        var el = new qx.ui.form.TextField();
        el.addListener("input", this.__modified, this);
        el.setRequired(true);
        el.tabFocus();
        form.add(el, this.tr("Name"), null, "name");

        el = new qx.ui.form.TextField();
        el.addListener("input", this.__modified, this);
        el.setRequired(true);
        form.add(el, this.tr("Full name"), null, "fullName");

        el = new qx.ui.form.TextField();
        el.addListener("input", this.__modified, this);
        el.setRequired(true);
        form.add(el, this.tr("E-Mail"), qx.util.Validate.email(), "email");

        el = new qx.ui.form.PasswordField();
        el.addListener("input", this.__modified, this);
        form.add(el, this.tr("Password"), null, "password");

        el = new qx.ui.form.PasswordField();
        el.addListener("input", this.__modified, this);
        form.add(el, this.tr("Password confirm"), this.__passwordCfrmValidator, "passwordCfrm", this);

        var fr = new sm.ui.form.FlexFormRenderer(form);
        fr._getLayout().setRowFlex(fr._row - 1, 1);
        this.add(fr, {flex: 1});

        var hcont = this.__buttonsCont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set(
            {"alignX": "right"}));
        hcont.setPadding(5);

        var bt = new qx.ui.form.Button(this.tr("Save"));
        bt.addListener("execute", this.__save, this);
        this.__broadcaster.attach(bt, "modified", "enabled");
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.__cancel, this);
        this.__broadcaster.attach(bt, "modified", "enabled");
        hcont.add(bt);

        this.add(hcont);
    },

    members: {

        __broadcaster: null,

        __form: null,

        __user: null,

        __buttonsCont: null,

        __editable: null,

        __modified: function() {
            this.__broadcaster.setModified(true);
        },

        setUser: function (name) {
            this.__user = name;
            this.__form.reset();
            var fitems = this.__form.getItems();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user",
                {name: this.__user}), "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                fitems["name"].setEnabled(false);
                fitems["name"].setValue(data["name"]);
                fitems["fullName"].setEnabled(this.__editable);
                fitems["fullName"].setValue(data["fullName"]);
                fitems["email"].setEnabled(this.__editable);

                fitems["email"].setValue(data["email"]);
                fitems["password"].setEnabled(this.__editable);

                fitems["password"].setRequired(false);
                fitems["password"].setValue(null);
                fitems["passwordCfrm"].setEnabled(this.__editable);
                fitems["passwordCfrm"].setRequired(false);
                fitems["passwordCfrm"].setValue(null);

                if (this.__editable) {
                    fitems["password"].show();
                    fitems["passwordCfrm"].show();
                    this.__buttonsCont.show();
                } else {
                    fitems["password"].exclude();
                    fitems["passwordCfrm"].exclude();
                    this.__buttonsCont.exclude();
                }
                this.__broadcaster.setModified(false);
            }, this);
        },

        __save: function () {
            if (!this.__form.validate() || !this.__editable) {
                return;
            }
            var fitems = this.__form.getItems();
            var uname = fitems["name"].getValue();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user",
                {name: uname}), "POST", "application/json");
            req.setParameter("email", fitems["email"].getValue());
            req.setParameter("fullname", fitems["fullName"].getValue());
            if (!!fitems["password"].getValue() && qx.lang.String.trimLeft(fitems["password"].getValue()) != "") {
                req.setParameter("password", fitems["password"].getValue());
            }
            req.send(function (resp) {
                this.fireDataEvent("userUpdated", resp.getContent());
                this.setUser(this.__user);
                ncms.Application.infoPopup(this.tr("User '%1' has been updated successfully", uname));
            }, this);
        },

        __cancel: function () {
            this.setUser(this.__user);
        },

        __passwordCfrmValidator: function (value, item) {
            var password = this.__form.getItems()["password"].getValue();
            if (!!password && !value) {
                item.setValid(false);
                item.setInvalidMessage(this.__form.getValidationManager().getRequiredFieldMessage());
                return false;
            } else if (value != password) {
                item.setValid(false);
                item.setInvalidMessage(this.tr("Not matching with password."));
                return false;
            }
            return true;
        }
    },

    destruct: function () {
        if (this.__broadcaster) {
            this.__broadcaster.destruct();
            this.__broadcaster = null;
        }
        this.__user = null;
        this.__editable = null;
        this.__buttonsCont = null;
    }
});
