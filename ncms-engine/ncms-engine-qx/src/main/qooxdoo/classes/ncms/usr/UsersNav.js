/**
 * Users navigation pane.
 */
qx.Class.define("ncms.usr.UsersNav", {
    extend : qx.ui.core.Widget,

    statics : {
        USER_EDITOR_CLAZZ : "ncms.usr.UserEditor"
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.setPaddingLeft(10);

        this.__selector = new ncms.usr.UserSelector();
        this.__selector.addListener("userSelected", this.__userSelected, this);
        this._add(this.__selector);

        var eclazz = ncms.usr.UsersNav.USER_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.usr.UserEditor();
        }, null, this);

        this.addListener("disappear", function() {
            //Navigation side is inactive so hide user editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
        }, this);
        this.addListener("appear", function() {
            if (app.getActiveWSAID() != eclazz && this.__selector.getSelectedUser() != null) {
                app.showWSA(eclazz);
            }
        }, this);

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
    },

    members : {

        __selector : null,

        __userSelected : function(ev) {
            var data = ev.getData();
            var app = ncms.Application.INSTANCE;
            var eclazz = ncms.usr.UsersNav.USER_EDITOR_CLAZZ;
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            app.getWSA(eclazz).setUserName(data["name"]);
            app.getWSA(eclazz).addListener("userUpdated", this.__userUpdated, this);
            app.showWSA(eclazz);
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("New user"));
            bt.addListenerOnce("execute", this.__onNewUser, this);
            menu.add(bt);

            var user = this.__selector.getSelectedUser();
            if (user !== null) {
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveUser, this);
                menu.add(bt);
            }
        },

        __onNewUser : function(ev) {
            var dlg = new ncms.usr.UserNewDlg();
            dlg.addListener("completed", function(ev) {
                dlg.close();
                this.__selector.reload();
            }, this);
            dlg.open();
        },

        __onRemoveUser : function(ev) {
            var user = this.__selector.getSelectedUser();
            if (user == null) {
                return;
            }
            ncms.Application.confirm(
                    this.tr("Are you sure to remove user: \"%1\"?", user["name"]),
                    function(yes) {
                        if (!yes) return;
                        var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user", {name : user["name"]}), "DELETE");
                        req.send(function(resp) {
                            this.__selector.reload();
                        }, this);
                    }, this);
        },

        __userUpdated : function(ev) {
            this.__selector.reloadData();
        }
    },

    destruct : function() {
    }
});