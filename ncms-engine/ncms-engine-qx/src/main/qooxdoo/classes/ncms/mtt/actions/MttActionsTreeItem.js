/**
 * Tree item for ncms.asm.am.TreeAMValueWidget
 */
qx.Class.define("ncms.mtt.actions.MttActionsTreeItem", {
    extend: qx.ui.tree.VirtualTreeItem,

    properties: {

        extra: {
            check: "String",
            event: "changeExtra",
            nullable: true
        }
    },

    construct: function () {
        this.base(arguments);
    },

    members: {

        _addWidgets: function () {
            this.addSpacer();
            this.addOpenButton();
            this.addIcon();
            this.addLabel();
            this.addWidget(new qx.ui.core.Spacer(), {flex: 1});
            var extra = this.getChildControl("extra");
            this.bind("extra", extra, "value");
            this.addWidget(extra);
            this.addWidget(new qx.ui.core.Spacer().set({width: 70}));
        },

        _createChildControlImpl: function (id, hash) {
            var control;
            switch (id) {
                case "extra":
                    control = new qx.ui.basic.Label().set({
                        alignY: "middle",
                        anonymous: true
                    });
                    break;
            }
            return control || this.base(arguments, id);
        }
    }
});