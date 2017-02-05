/**
 * Tracking pixels selector.
 */
qx.Class.define("ncms.mtt.tp.MttTpSelector", {
    extend: qx.ui.container.Composite,

    events: {

        /**
         * Event fired if mtt tracking pixel was selected/deselected
         *
         *
         * DATA: {
         *      "id" : {Number} Tracking pixel id.
         *      "name": {String} Tracking pixel name
         * }
         *
         * or `null` if noe selection
         *
         */
        "tpSelected": "qx.event.type.Data"
    },


    properties: {

        appearance: {
            refine: true,
            init: "ncms-mtt-tp-selector"
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

        this.__table = new ncms.mtt.tp.MttTpTable(useColumns, smodel, toolbarFn);
        this.__table.getSelectionModel().addListener("changeSelection", function () {
            var tp = this.getSelectedTp();
            if (this.__prevTp != tp) {
                this.fireDataEvent("tpSelected", tp ? tp : null);
                this.__prevTp = tp;
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

        __prevTp: null,

        /**
         * Search field
         * @type {sm.ui.form.SearchField}
         */
        __sf: null,

        /**
         * Tp virtual table
         * @type {ncms.mtt.tp.MttTpTable}
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

        getSelectedTpInd: function () {
            return this.__table.getSelectedTpInd();
        },

        getSelectedTp: function () {
            return this.__table.getSelectedTp();
        },

        getSelectedTps: function () {
            return this.__table.getSelectedTps();
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
        this.__prevTp = null;
    }
});