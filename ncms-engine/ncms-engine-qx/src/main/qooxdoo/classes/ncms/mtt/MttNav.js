/**
 * Marketing traffic Rules navigation zone.
 */

qx.Class.define("ncms.mtt.MttNav", {
    extend: qx.ui.core.Widget,

    statics: {
        MTT_EDITOR_CLAZZ: "ncms.mtt.MttEditor"
    },

    events: {},

    properties: {},

    construct: function () {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.setPaddingLeft(10);
        this.__selector = new ncms.mtt.MttRulesSelector();
        this.__selector.addListener("ruleSelected", this.__ruleSelected, this);
        this._add(this.__selector);

        var eclazz = ncms.mtt.MttNav.MTT_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.mtt.MttEditor();
        }, null, this);

        this.addListener("disappear", function () {
            //Navigation side is inactive so hide mtt editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
            //app.disposeWSA(eclazz);
        }, this);
        this.addListener("appear", function () {
            if (app.getActiveWSAID() != eclazz && this.__selector.getSelectedRule() != null) {
                app.showWSA(eclazz);
            }
        }, this);

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
    },

    members: {

        __selector: null,

        __ruleSelected: function (ev) {
            var data = ev.getData();
            var app = ncms.Application.INSTANCE;
            var eclazz = ncms.mtt.MttNav.MTT_EDITOR_CLAZZ;
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            app.getWSA(eclazz).setRuleId(data["id"]);
            app.showWSA(eclazz);
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("New rule"));
            bt.addListenerOnce("execute", this.__onNewRule, this);
            menu.add(bt);

            bt = new qx.ui.menu.Button(this.tr("Refresh"));
            bt.addListenerOnce("execute", this.__onRefresh, this);
            menu.add(bt);

            var rind = this.__selector.getSelectedRuleInd();
            if (rind !== -1) {

                bt = new qx.ui.menu.Button(this.tr("Rename"));
                bt.addListenerOnce("execute", this.__onRenameRule, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Move up"));
                bt.addListenerOnce("execute", this.__onMoveUp, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Move down"));
                bt.addListenerOnce("execute", this.__onMoveDown, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Disable"));
                bt.addListenerOnce("execute", this.__onToggleDisabled, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveRule, this);
                menu.add(bt);
            }
        },

        __onNewRule: function (ev) {
            var dlg = new ncms.mtt.MttRuleNewDlg();
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this.__selector.reload();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
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
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.show();
        },

        __onToggleDisabled: function (ev) {
            //todo
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
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl(
                    dir > 0 ? "mtt.rules.up" : "mtt.rules.down", {id: rule["id"]}), "POST");
            req.send(function (resp) {
                var table = this.__selector.getTable();
                table.getTableModel().reloadData();
                table.selectSingleRow(nind);
            }, this);
        }
    },

    destruct: function () {
        this.__selector = null;
    }
});

