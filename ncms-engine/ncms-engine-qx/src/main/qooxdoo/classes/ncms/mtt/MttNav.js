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
            //Navigation side is inactive so hide ntt editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
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

                //todo
                bt = new qx.ui.menu.Button(this.tr("Move up"));
                bt.addListenerOnce("execute", this.__onMoveUp, this);
                menu.add(bt);

                //todo
                bt = new qx.ui.menu.Button(this.tr("Move down"));
                bt.addListenerOnce("execute", this.__onMoveDown, this);
                menu.add(bt);

                //todo disable/enable
                bt = new qx.ui.menu.Button(this.tr("Disable"));
                bt.addListenerOnce("execute", this.__onToggleDisabled, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveRule, this);
                menu.add(bt);
            }
        },

        __onNewRule: function (ev) {
            var dlg = new ncms.mtt.RuleNewDlg();
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                var spec = ev.getData();
                this.__selector.setSearchBoxValue(spec["name"]);
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.show();
        },

        __onRemoveRule: function (ev) {

        },

        __onToggleDisabled: function (ev) {

        },

        __onRenameRule: function (ev) {

        },

        __onRefresh: function () {
            this.__selector.reload();
        },

        __onMoveUp: function () {

        },

        __onMoveDown: function () {

        }
    },

    destruct: function () {
        this.__selector = null;
    }
});

