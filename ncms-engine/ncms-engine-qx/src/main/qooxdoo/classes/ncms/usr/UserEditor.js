/**
 * User editor pane
 */
qx.Class.define("ncms.usr.UserEditor", {
    extend: qx.ui.tabview.TabView,

    statics: {},

    events: {
        /**
         * Data: updated user data.
         */
        "userUpdated": "qx.event.type.Data"
    },

    properties: {
        /**
         * User name to load in editor
         */
        "userName": {
            apply: "__applyUserName",
            nullable: true,
            check: "String"
        }
    },

    construct: function (userEditable, accessEditable) {
        var prevFocus = qx.ui.core.FocusHandler.getInstance().getFocusedWidget();

        this.base(arguments, "top");
        this.setPadding(5);

        var epage = new qx.ui.tabview.Page(this.tr("Roles/Groups"));
        epage.setLayout(new qx.ui.layout.VBox());
        var urTable = this.__userRoles = new ncms.usr.UserRolesTable(accessEditable);
        epage.add(urTable);

        this.add(epage);

        epage = new qx.ui.tabview.Page(this.tr("Edit user"));
        epage.setLayout(new qx.ui.layout.VBox());
        var uiForm = this.__userInfoForm = new ncms.usr.UserEditForm(userEditable);
        uiForm.addListener("userUpdated", this.__userUpdated, this);
        epage.add(uiForm);

        this.add(epage);

        // qx.ui.tabview.TabView automatically sets focus on the first tab after initialization
        window.setTimeout(function () {
            var curFocus = qx.ui.core.FocusHandler.getInstance().getFocusedWidget();
            if (prevFocus != null && prevFocus !== curFocus) {
                prevFocus.focus();
            }
            prevFocus = null;
            curFocus = null;
        }, 0);
    },

    members: {
        __userRoles: null,
        __userInfoForm: null,

        __applyUserName: function (userName) {
            this.__userRoles.setUser(userName);
            this.__userInfoForm.setUser(userName);
        },

        __userUpdated: function (ev) {
            this.fireDataEvent("userUpdated", ev.getData());
        }
    },

    destruct: function () {
    }
});
