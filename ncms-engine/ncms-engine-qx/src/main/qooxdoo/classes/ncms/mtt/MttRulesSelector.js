/**
 * Traffic rules selector.
 */
qx.Class.define("ncms.mtt.MttRulesSelector", {
    extend: qx.ui.container.Composite,

    events: {

        /**
         * Event fired if mtt rule was selected/deselected
         *
         * DATA: {
         *        "id" : {Number} Rule id.
         *        "name" : {String} Rule name,
         *        "type" : {String} Rule type,
         *       };
         * or `null` if noe selection
         */
        "ruleSelected": "qx.event.type.Data"
    },

    properties: {

        appearance: {
            refine: true,
            init: "ncms-mtt-rules-selector"
        }
    },

    construct: function (constViewSpec, smodel, useColumns, toolbarFn) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function () {
            this.__search(null);
        }, this);
        sf.addListener("input", function (ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("keypress", this.__searchKeypress, this);

        this.__table = new ncms.mtt.MttRulesTable(useColumns, smodel, toolbarFn);
        this.__table.getSelectionModel().addListener("changeSelection", function () {
            var rule = this.getSelectedRule();
            if (this.__prevRule != rule) {
                this.fireDataEvent("ruleSelected", rule ? rule : null);
                this.__prevRule = rule;
            }
        }, this);

        this._add(this.__sf);
        this._add(this.__table, {flex: 1});

        this.setConstViewSpec(constViewSpec || null);
        this.addListener("appear", function () {
            sf.focus();
        });
    },

    members: {

        __prevRule: null,

        /**
         * Search field
         * @type {sm.ui.form.SearchField}
         */
        __sf: null,

        /**
         * Rules virtual table
         * @type {ncms.mtt.MttRulesTable}
         */
        __table: null,

        setSearchBoxValue: function (val) {
            this.__sf.setValue(val);
            this.__search(val);
        },

        setViewSpec: function (vs) {
            this.__table.getTableModel().setViewSpec(vs);
        },

        updateViewSpec: function (vs) {
            this.__table.getTableModel().updateViewSpec(vs);
        },

        setConstViewSpec: function (vs, noupdate) {
            this.__table.getTableModel().setConstViewSpec(vs, noupdate);
        },

        reload: function (vspec) {
            this.__table.getTableModel().reloadData(vspec);
        },

        resetSelection: function () {
            this.__table.resetSelection();
        },

        getSelectedRuleInd: function () {
            return this.__table.getSelectedRuleInd();
        },

        getSelectedRule: function () {
            return this.__table.getSelectedRule();
        },

        getSelectedRules: function () {
            return this.__table.getSelectedRules();
        },

        cleanup: function () {
            this.__table.cleanup();
        },

        getToolbarTable: function () {
            return this.__table;
        },

        getTable: function () {
            return this.getToolbarTable().getTable();
        },

        getRowCount: function () {
            return this.__table.getRowCount();
        },

        __search: function (val) {
            this.updateViewSpec({stext: val || ""});
        },

        __applyConstViewSpec: function () {
            this.__search();
        },

        __searchKeypress: function (ev) {
            if ("Down" === ev.getKeyIdentifier()) {
                this.__table.getTable().handleFocus();
            }
        }
    },

    destruct: function () {
        this.__sf = null;
        this.__table = null;
        this.__prevRule = null;
        //this._disposeObjects("__form");
    }
});