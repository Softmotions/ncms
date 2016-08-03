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
            nullable: true
        },

        groupWeight: {
            check: "Number",
            event: "changeGroupWeight",
            nullable: true
        },

        ptype: {
            check: "String",
            event: "changePtype",
            apply: "__applyPtype",
            nullable: true
        }
    },

    construct: function () {
        this.base(arguments);
    },

    members: {

        __applyPtype: function (val) {
            var w = this.getChildControl("weight", true);
            if (val === "group") {
                w.show();
            } else {
                w.exclude();
            }
        },

        __applyActive: function (val) {
            var icon = this.getChildControl("icon", true);
            if (icon) {
                icon.setOpacity(val ? 1.0 : 0.2);
            }
        },

        _addWidgets: function () {
            this.addSpacer();
            this.addOpenButton();
            this.addIcon();

            var w = this.getChildControl("weight");
            this.bind("groupWeight", w, "value");
            w.bind("value", this, "groupWeight");
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
                    control = new qx.ui.form.Spinner(0, 0, 100).set({
                        focusable: false,
                        allowGrowY: false,
                        alignY: "middle",
                        singleStep: 10,
                        marginRight: 4,
                        numberFormat: new qx.util.format.NumberFormat().set({
                            postfix: "%"
                        })
                    });
                    control.getChildControl("textfield").set({
                        readOnly: true,
                        selectable: false
                    });
                    control.addListener("dbltap", function (ev) {
                        ev.stopPropagation();
                    });
                    break;
            }
            return control || this.base(arguments, id);
        }
    }
});