/**
 * Tracking pixels navigation side.
 */
qx.Class.define("ncms.mtt.tp.MttTpNav", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.MCommands],

    statics: {
        MTT_EDITOR_CLAZZ: "ncms.mtt.tp.MttTpEditor"
    },

    construct: function () {
        var me = this;
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.setPaddingLeft(10);

        var eclazz = ncms.mtt.tp.MttTpNav.MTT_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.mtt.tp.MttTpEditor();
        }, null, this);

        this.addListener("appear", function () {
            if (app.getActiveWSAID() != eclazz) {
                if (this.__selector.getSelectedTp() != null) {
                    app.showWSA(eclazz);
                } else {
                    app.showDefaultWSA();
                }
            }
        }, this);

        this.__selector = new ncms.mtt.tp.MttTpSelector();
        this.__selector.addListener("tpSelected", this.__tpSelected, this);
        this._add(this.__selector);

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNewTp, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onRemoveTp, this);
        this._registerCommandFocusWidget(this.__selector.getTable());
    },

    members: {

        __removeBt: null,

        __selector: null,

        __tpSelected: function (ev) {
            var data = ev.getData();
            var app = ncms.Application.INSTANCE;
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            var eclazz = ncms.mtt.tp.MttTpNav.MTT_EDITOR_CLAZZ;
            app.getWSA(eclazz).setTp(data);
            app.showWSA(eclazz);
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("New tracking pixel"));
            bt.addListenerOnce("execute", this.__onNewTp, this);
            menu.add(bt);

            var tp = this.__selector.getSelectedTp();
            if (tp != null) {
                bt = new qx.ui.menu.Button(this.tr("Rename"));
                bt.addListenerOnce("execute", this.__onRenameTp, this);
                menu.add(bt);

                if (tp.enabled) {
                    bt = new qx.ui.menu.Button(this.tr("Disable"));
                    bt.addListenerOnce("execute", this.__onDisable, this);
                    menu.add(bt);
                } else {
                    bt = new qx.ui.menu.Button(this.tr("Enabled"));
                    bt.addListenerOnce("execute", this.__onEnable, this);
                    menu.add(bt);
                }


                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveTp, this);
                menu.add(bt);
            }

            menu.add(new qx.ui.menu.Separator());
            bt = new qx.ui.menu.Button(this.tr("Refresh"));
            bt.addListenerOnce("execute", this.__onRefresh, this);
            menu.add(bt);
        },

        __onNewTp: function (ev) {
            var dlg = new ncms.mtt.tp.MttTpNewDlg();
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this.__selector.reload();
                this.__selector.getTable().handleFocus();
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(this.__selector.getTable(), false);
            }
            dlg.show();
        },

        __onRefresh: function () {
            this.__selector.reload();
        },

        __onRenameTp: function (ev) {
            var tp = this.__selector.getSelectedTp();
            if (tp == null) {
                return;
            }
            var dlg = new ncms.mtt.tp.MttTpRenameDlg(tp["id"], tp["name"]);
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this.__selector.reload();
                this.__selector.getTable().handleFocus();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.show();
        },

        __onDisable: function () {
            this.__disableEnable(false);
        },

        __onEnable: function () {
            this.__disableEnable(true);
        },

        __disableEnable: function (enabled) {
            var tp = this.__selector.getSelectedTp();
            if (tp == null) {
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl(
                    enabled ? "mtt.tp.enable" : "mtt.tp.disable", {id: tp["id"]}), "POST");
            req.send(function (resp) {
                this.__selector.reload();
                this.__selector.getTable().handleFocus();
            }, this);
        },

        __onRemoveTp: function (ev) {
            var tp = this.__selector.getSelectedTp();
            if (tp == null) {
                return;
            }
            ncms.Application.confirm(
                this.tr("Are you sure to remove tracking pixel: \"%1\"?", tp["name"]),
                function (yes) {
                    if (!yes) return;
                    var req = new sm.io.Request(
                        ncms.Application.ACT.getRestUrl("mtt.tp.delete", {id: tp["id"]}), "DELETE");
                    req.send(function (resp) {
                        this.__selector.resetSelection();
                        this.__selector.reload();
                    }, this);
                }, this);
        }
    },

    destruct: function () {
        this.__removeBt = null;
        this.__selector = null;
    }

});
