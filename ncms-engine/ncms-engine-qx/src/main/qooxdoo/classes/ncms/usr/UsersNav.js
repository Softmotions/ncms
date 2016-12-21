/**
 * Users navigation pane.
 */
qx.Class.define("ncms.usr.UsersNav", {
    extend: qx.ui.tabview.TabView,

    include: [ncms.cc.MCommands],

    statics: {
        USER_EDITOR_CLAZZ: "ncms.usr.UserEditor"
    },

    events: {},

    properties: {},

    construct: function () {
        this.base(arguments, "top");
        this.set({paddingTop: 5, paddingBottom: 5});

        this.__selectors = [];
        var page = new qx.ui.tabview.Page(this.tr("All"));
        page.setLayout(new qx.ui.layout.Grow());
        var us = new ncms.usr.UserSelector();
        this.__selectors.push(us);
        us.addListener("userSelected", this.__userSelected, this);
        page.add(us);
        this.add(page);

        page = new qx.ui.tabview.Page(this.tr("Active only"));
        page.setLayout(new qx.ui.layout.Grow());
        us = new ncms.usr.UserSelector({onlyActive: true});
        this.__selectors.push(us);
        us.addListener("userSelected", this.__userSelected, this);
        page.add(us);
        this.add(page);

        var req = new sm.io.Request(ncms.Application.ACT.getUrl("security.settings"), "GET", "application/json");
        req.setAsynchronous(false);
        req.send(function (resp) {
            var settings = resp.getContent() || {};
            var userEditable = !!settings["usersWritable"];
            var accessEditable = !!settings["usersAccessWritable"];

            var eclazz = ncms.usr.UsersNav.USER_EDITOR_CLAZZ;
            var app = ncms.Application.INSTANCE;
            app.registerWSA(eclazz, function () {
                return new ncms.usr.UserEditor(userEditable, accessEditable);
            }, null, this);

            this.addListener("appear", function () {
                if (app.getActiveWSAID() != eclazz) {
                    if (this.__getSelectedUser() != null) {
                        app.showWSA(eclazz);
                    } else {
                        app.showDefaultWSA();
                    }
                }
            }, this);

            if (userEditable) {
                this.setContextMenu(new qx.ui.menu.Menu());
                this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            }
        }, this);

        this.addListener("changeSelection", function (ev) {
            ev.getData()[0].getChildren()[0].reload();
        }, this);

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNewUser, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onRemoveUser, this);
        this._registerCommandFocusWidget(this);
    },

    members: {

        __selectors: null,

        __userSelected: function (ev) {
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

        __getSelectedUser: function () {
            return this.getSelection()[0].getChildren()[0].getSelectedUser();
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("New user"));
            bt.addListenerOnce("execute", this.__onNewUser, this);
            menu.add(bt);

            var user = this.__getSelectedUser();
            if (user !== null) {
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveUser, this);
                menu.add(bt);
            }
        },

        __onNewUser: function (ev) {
            var dlg = new ncms.usr.UserNewDlg();
            dlg.addListener("completed", function (ev) {
                dlg.close();
                for (var i = 0; i < this.__selectors.length; ++i) {
                    this.__selectors[i].reload();
                }
            }, this);
            dlg.open();
        },

        __onRemoveUser: function (ev) {
            var user = this.__getSelectedUser();
            if (user == null) {
                return;
            }
            ncms.Application.confirm(
                this.tr("Are you sure to remove user: \"%1\"?", user["name"]),
                function (yes) {
                    if (!yes) return;
                    var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user",
                        {name: user["name"]}), "DELETE");
                    req.send(function (resp) {
                        for (var i = 0; i < this.__selectors.length; ++i) {
                            this.__selectors[i].reload();
                        }
                    }, this);
                }, this);
        },

        __userUpdated: function (ev) {
            for (var i = 0; i < this.__selectors.length; ++i) {
                this.__selectors[i].reloadData();
            }
        }
    },

    destruct: function () {
        this.__selectors = null;
    }
});