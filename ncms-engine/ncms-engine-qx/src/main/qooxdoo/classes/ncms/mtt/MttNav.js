/**
 * Marketing traffic Rules navigation zone.
 *
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 */

qx.Class.define("ncms.mtt.MttNav", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.MCommands],

    statics: {
        MTT_EDITOR_CLAZZ: "ncms.mtt.MttEditor"
    },

    construct: function () {
        var me = this;
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.setPaddingLeft(10);

        this.__selector = new ncms.mtt.MttRulesSelector(null, null, null, function (toolbar) {
            function _createButton(label, icon, handler, self) {
                var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
                if (handler != null) {
                    bt.addListener("execute", handler, self);
                }
                return bt;
            }

            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            el = _createButton(null, "ncms/icon/16/actions/add.png", me.__onNewRule, me);
            el.setToolTipText(this.tr("New rule"));
            part.add(el);

            el = _createButton(null, "ncms/icon/16/actions/delete.png", me.__onRemoveRule, me);
            el.setToolTipText(this.tr("Remove"));
            el.setEnabled(false);
            me.__removeBt = el;
            part.add(el);


            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});

            part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            // Move up
            var el = _createButton(null, "ncms/icon/16/misc/arrow_up.png", me.__onMoveUp, me);
            el.setToolTipText(this.tr("Move item up"));
            el.setEnabled(false);
            me.__moveUpBt = el;
            part.add(el);

            // Move down
            el = _createButton(null, "ncms/icon/16/misc/arrow_down.png", me.__onMoveDown, me);
            el.setToolTipText(this.tr("Move item down"));
            part.add(el);
            el.setEnabled(false);
            me.__moveDownBt = el;
            return toolbar;
        });

        this.__selector.addListener("ruleSelected", this.__ruleSelected, this);
        this._add(this.__selector);

        var eclazz = ncms.mtt.MttNav.MTT_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.mtt.MttEditor();
        }, null, this);

        this.addListener("appear", function () {
            if (app.getActiveWSAID() != eclazz) {
                if (this.__selector.getSelectedRule() != null) {
                    app.showWSA(eclazz);
                } else {
                    app.showDefaultWSA();
                }
            }
        }, this);

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNewRule, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onRemoveRule, this);
        this._registerCommandFocusWidget(this.__selector.getTable());
    },

    members: {

        __removeBt: null,
        __moveUpBt: null,
        __moveDownBt: null,

        __selector: null,

        __ruleSelected: function (ev) {
            var data = ev.getData();
            var app = ncms.Application.INSTANCE;
            this.__removeBt.setEnabled(data != null);
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            var ind = this.__selector.getSelectedRuleInd();
            var rc = this.__selector.getRowCount();
            this.__moveUpBt.setEnabled(ind > 0);
            this.__moveDownBt.setEnabled(ind + 1 < rc);

            var eclazz = ncms.mtt.MttNav.MTT_EDITOR_CLAZZ;
            app.getWSA(eclazz).setRuleId(data["id"]);
            app.showWSA(eclazz);
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("New rule"));
            bt.addListenerOnce("execute", this.__onNewRule, this);
            menu.add(bt);

            var rule = this.__selector.getSelectedRule();
            if (rule != null) {
                // {"cdate":1469886776824,"mdate":1469886776824,"name":"Тест1",
                // "flags":0,"id":41,"enabled":true,"ordinal":41}
                bt = new qx.ui.menu.Button(this.tr("Rename"));
                bt.addListenerOnce("execute", this.__onRenameRule, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Move up"));
                bt.addListenerOnce("execute", this.__onMoveUp, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Move down"));
                bt.addListenerOnce("execute", this.__onMoveDown, this);
                menu.add(bt);

                if (rule.enabled) {
                    bt = new qx.ui.menu.Button(this.tr("Disable"));
                    bt.addListenerOnce("execute", this.__onDisable, this);
                    menu.add(bt);
                } else {
                    bt = new qx.ui.menu.Button(this.tr("Enabled"));
                    bt.addListenerOnce("execute", this.__onEnable, this);
                    menu.add(bt);
                }

                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveRule, this);
                menu.add(bt);
            }

            menu.add(new qx.ui.menu.Separator());
            bt = new qx.ui.menu.Button(this.tr("Refresh"));
            bt.addListenerOnce("execute", this.__onRefresh, this);
            menu.add(bt);
        },

        __onNewRule: function (ev) {
            var dlg = new ncms.mtt.MttRuleNewDlg();
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

        __onRemoveRule: function (ev) {
            var rule = this.__selector.getSelectedRule();
            if (rule == null) {
                return;
            }
            ncms.Application.confirm(
                this.tr("Are you sure to remove rule: \"%1\"?", rule["name"]),
                function (yes) {
                    if (!yes) return;
                    var req = new sm.io.Request(
                        ncms.Application.ACT.getRestUrl("mtt.rules.delete", {id: rule["id"]}), "DELETE");
                    req.send(function (resp) {
                        this.__selector.reload();
                        this.__selector.getTable().handleFocus();
                    }, this);
                }, this);
        },

        __onRenameRule: function (ev) {
            var rule = this.__selector.getSelectedRule();
            if (rule == null) {
                return;
            }
            var dlg = new ncms.mtt.MttRuleRenameDlg(rule["id"], rule["name"]);
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
            var rule = this.__selector.getSelectedRule();
            if (rule == null) {
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl(
                    enabled ? "mtt.rule.enable" : "mtt.rule.disable", {id: rule["id"]}), "POST");
            req.send(function (resp) {
                this.__selector.reload();
                this.__selector.getTable().handleFocus();
            }, this);
        },

        __onRefresh: function () {
            this.__selector.reload();
        },

        __onMoveUp: function () {
            return this.__onMove(1);
        },

        __onMoveDown: function () {
            return this.__onMove(-1);
        },

        __onMove: function (dir) {
            var ind = this.__selector.getSelectedRuleInd();
            var rc = this.__selector.getRowCount();
            var nind = (dir > 0) ? --ind : ++ind;
            if (ind == -1 || nind < 0 || nind >= rc) {
                return;
            }
            var rule = this.__selector.getSelectedRule();
            if (rule == null) {
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl(
                    dir > 0 ? "mtt.rule.up" : "mtt.rule.down", {id: rule["id"]}), "POST");
            req.send(function (resp) {
                var table = this.__selector.getTable();
                table.getTableModel().reloadData();
                table.getTableModel().addListenerOnce("rowsDataLoaded", function () {
                    table.selectSingleRow(ind);
                });
            }, this);
        }
    },

    destruct: function () {
        this.__removeBt = null;
        this.__selector = null;
        this.__moveDownBt = null;
        this.__moveUpBt = null;
    }
});

