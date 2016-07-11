/**
 * Marketing traffic Rules editor.
 */
qx.Class.define("ncms.mtt.MttEditor", {
    extend: qx.ui.core.Widget,

    events: {},

    properties: {

        /**
         * Rule ID to show
         */
        "ruleId": {
            apply: "__applyRuleId",
            nullable: true,
            check: "Number"
        },

        /**
         * Set a rule JSON representation
         * of com.softmotions.ncms.mt.MttRule
         */
        "ruleSpec": {
            apply: "__applyRuleSpec",
            nullable: true,
            check: "Object"
        }
    },

    construct: function () {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(5));
        this.setBackgroundColor("red");
    },

    members: {

        __applyRuleId: function (val, old) {
            if (val == null) {
                this.setRuleSpec(null);
                return;
            }
            this.__reload();
        },

        __applyRuleSpec: function (spec) {

        },

        __reload: function () {

        }

    },

    destruct: function () {
        //this._disposeObjects("__form");
    }
});

