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
        },

        active: {
            check: "Boolean",
            event: "changeEnabled",
            nullable: false,
            init: true,
            apply: "__applyActive"
        },

        groupId: {
            check: "Number",
            event: "changeGroupId",
            apply: "__applyGroupId",
            nullable: true
        },

        groupWeight: {
            check: "Number",
            event: "changeGroupWeight",
            nullable: true
        }
    },

    construct: function () {
        this.base(arguments);
    },

    members: {

        __applyActive: function (val) {
            var icon = this.getChildControl("icon", true);
            if (icon) {
                icon.setOpacity(val ? 1.0 : 0.2);
            }
        },

        __applyGroupId: function (val) {
            var w = this.getChildControl("weight", true);
            if (w) {
                if (val > 0) {
                    w.show();
                } else {
                    w.exclude();
                }
            }
        },

        _addWidgets: function () {
            this.addSpacer();
            this.addOpenButton();
            this.addIcon();

            var w = this.getChildControl("weight");
            w.set({
                focusable: false,
                allowGrowY: false,
                alignY: "middle",
                singleStep: 10,
                marginRight: 4
            });
            w.getChildControl("textfield").set({
                readOnly: true,
                selectable: false
            });
            this.bind("groupWeight", w, "value");
            w.bind("value", this, "groupWeight");
            w.addListener("dbltap", function(ev) {
                ev.stopPropagation();
            });
            this.addWidget(w);
            w.exclude();

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
                case "weight":
                    control = new qx.ui.form.Spinner(0, 0, 100);
                    break;
            }
            return control || this.base(arguments, id);
        }
    }
});