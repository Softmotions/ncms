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

        this.add(new qx.ui.tabview.Page(this.tr("Roles/Groups")));
        this.add(new qx.ui.tabview.Page(this.tr("General")));

        // TODO:

    },

    members : {
        __applyUserName : function(userName) {
            // TODO:
        }
    },

    destruct : function() {
    }
});
