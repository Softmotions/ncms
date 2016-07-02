/**
 * User selector
 */
qx.Class.define("ncms.usr.UserSelectorDlg", {
    extend: qx.ui.window.Window,

    statics: {},

    events: {
        /**
         * Data: [] array of selected users.
         * @see ncms.usr.UserSelector
         */
        "completed": "qx.event.type.Data"
    },

    properties: {},

    construct: function (caption, icon, constViewSpec, smodel) {
        this.base(arguments, caption, icon);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });
        var selector = this.__selector = new ncms.usr.UserSelector(constViewSpec, smodel);
        this.add(selector, {flex: 1});

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

        cmd = this.createCommand("Enter");
        cmd.addListener("execute", this.__ok, this);

        this.addListenerOnce("resize", this.center, this);
        selector.addListener("userSelected", this.__syncState, this);
        selector.getTable().addListener("cellDbltap", this.__ok, this);

        this.__syncState();
    },

    members: {
        /**
         * Users selector
         */
        __selector: null,

        /**
         * Okay button
         */
        __okBt: null,

        __ok: function () {
            if (!this.__okBt.getEnabled()) {
                return;
            }
            this.fireDataEvent("completed", this.__selector.getSelectedUsers())
        },

        __syncState: function () {
            var user = this.__selector.getSelectedUser();
            this.__okBt.setEnabled(user != null);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this.__selector = null;
        this.__okBt = null;
    }
});