/**
 * Traffic rules selector.
 */
qx.Class.define("ncms.mtt.MttRulesSelector", {
    extend: qx.ui.core.Widget,

    events: {

        /**
         * Event fired if mtt rule was selected/deselected
         *
         * DATA: var item = {
         *        "id" : {Number} Rule id.
         *        "name" : {String} Rule name,
         *        "type" : {String} Rule type,
         *       };
         * or null if selection cleared
         */
        "ruleSelected": "qx.event.type.Data"
    },

    properties: {

        appearance: {
            refine: true,
            init: "ncms-mtt-rules-selector"
        }
    },

    construct: function (constViewSpec, smodel, useColumns) {
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

        this.__table = new ncms.mtt.MttRulesTable(useColumns).set({
            "statusBarVisible": true,
            "showCellFocusIndicator": false
        });
        if (smodel != null) {
            this.__table.setSelectionModel(smodel);
        }
        this.__table.getSelectionModel().addListener("changeSelection", function () {
            var rule = this.getSelectedRule();
            this.fireDataEvent("ruleSelected", rule ? rule : null);
        }, this);

        this._add(this.__sf);
        this._add(this.__table, {flex: 1});

        this.setConstViewSpec(constViewSpec || null);
        this.addListener("appear", function () {
            sf.focus();
        });
    },

    members: {

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
            this.__table.resetSelection();
            this.__table.getTableModel().setViewSpec(vs);
        },

        updateViewSpec: function (vs) {
            this.__table.resetSelection();
            this.__table.getTableModel().updateViewSpec(vs);
        },

        setConstViewSpec: function (vs, noupdate) {
            this.__table.resetSelection();
            this.__table.getTableModel().setConstViewSpec(vs, noupdate);
        },

        reload: function (vspec) {
            this.__table.getTableModel().reloadData(vspec);
            this.__table.resetSelection();
        },

        resetSelection: function () {
            this.__table.resetSelection();
        },

        getTable: function () {
            return this.__table;
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

        getRowCount: function() {
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
                this.__table.handleFocus();
            }
        }
    },

    destruct: function () {
        this.__sf = null;
        this.__table = null;
        //this._disposeObjects("__form");
    }
});