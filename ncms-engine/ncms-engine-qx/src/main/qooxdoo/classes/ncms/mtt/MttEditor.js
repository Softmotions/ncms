/**
 * Marketing traffic Rules editor.
 */
qx.Class.define("ncms.mtt.MttEditor", {
    extend: qx.ui.core.Widget,

    properties: {

        /**
         * Rule ID to show
         */
        "ruleId": {
            apply: "__applyRuleId",
            nullable: true,
            check: "Number"
        }
    },

    construct: function () {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());

        this.__filters = new ncms.mtt.filters.MttFiltersTable(this.tr("Filters"));
        this.__actions = new ncms.mtt.actions.MttActionsTree(this.tr("Actions"));

        var sp = new qx.ui.splitpane.Pane("vertical");
        sp.add(this.__filters, 1);
        sp.add(this.__actions, 3);
        this._add(sp);
    },

    members: {

        __filters: null,

        __actions: null,

        __applyRuleId: function (val) {
            this.__filters.setRuleId(val);
            this.__actions.setRuleId(val);
        }
    },

    destruct: function () {
        this.__filters = null;
        this.__actions = null;
        //this._disposeObjects("__form");
    }
});

