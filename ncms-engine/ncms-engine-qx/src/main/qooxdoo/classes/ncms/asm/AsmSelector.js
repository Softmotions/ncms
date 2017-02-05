/**
 * Selector of assemblies
 * with included search text box
 * and assembly table.
 */
qx.Class.define("ncms.asm.AsmSelector", {
    extend: qx.ui.core.Widget,

    events: {

        /**
         * Event fired if assembly was selected/deselected
         *
         * DATA: var item = {
         *        "id" : {Number} Assembly id.
         *        "name" : {String} Page name,
         *        "type" : {String} Assembly type,
         *        "template": {Boolean?} Assembly is a template?
         *        "exclude": {Number?} Exclude the assembly with specified id
         *       };
         * or null if selection cleared
         */
        "asmSelected": "qx.event.type.Data"
    },

    properties: {

        appearance: {
            refine: true,
            init: "ncms-asm-selector"
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


        this.__table = new ncms.asm.AsmTable(useColumns).set({
            "statusBarVisible": true,
            "showCellFocusIndicator": false
        });
        if (smodel != null) {
            this.__table.setSelectionModel(smodel);
        }
        this.__table.getSelectionModel().addListener("changeSelection", function () {
            var asm = this.getSelectedAsm();
            this.fireDataEvent("asmSelected", asm ? asm : null);
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
         * Assemblies virtual table
         * @type {ncms.asm.AsmTable}
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
            this.__table.getTableModel().reloadData();
        },

        resetSelection: function () {
            this.__table.resetSelection();
        },

        getTable: function () {
            return this.__table;
        },

        getSelectedAsmInd: function () {
            return this.__table.getSelectedAsmInd();
        },

        getSelectedAsm: function () {
            return this.__table.getSelectedAsm();
        },

        getSelectedAsms: function () {
            return this.__table.getSelectedAsms();
        },

        cleanup: function () {
            this.__table.cleanup();
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
    }
});