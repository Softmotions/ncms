/**
 * User editor pane
 */
qx.Class.define("ncms.usr.UserEditor", {
    extend : qx.ui.tabview.TabView,

    statics : {
    },

    events : {
    },

    properties : {
        /**
         * User name to load in editor
         */
        "userName" : {
            apply : "__applyUserName",
            nullable : true,
            check : "String"
        }
    },

    construct : function() {
        this.base(arguments, "top");

        var epage = new qx.ui.tabview.Page(this.tr("Roles/Groups"));
        epage.setLayout(new qx.ui.layout.VBox());
        var urTable = this.__userRoles = new ncms.usr.UserRolesTable();
        epage.add(urTable);

        this.add(epage);

        this.add(new qx.ui.tabview.Page(this.tr("General")));
        // TODO: general tab

    },

    members : {
        __userRoles : null,

        __applyUserName : function(userName) {
            this.__userRoles.setUser(userName);
            // TODO: set user in general tab
        }
    },

    destruct : function() {
    }
});
