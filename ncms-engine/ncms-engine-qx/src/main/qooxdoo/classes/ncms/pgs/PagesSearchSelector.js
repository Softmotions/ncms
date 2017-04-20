/**
 * Pages selector with search (plain table)
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.pgs.PagesSearchSelector", {
    extend: qx.ui.core.Widget,

    events: {
        /**
         * DATA: var item = {
         *        "id"          : {Object} Optional Node ID
         *        "label"       : {String} Item name.
         *        "accessMask"  : {String} Page access mask.
         *       };
         * or null if selection cleared
         */
        itemSelected: "qx.event.type.Data"
    },

    properties: {

        /**
         * If true - search list will be populated
         * even if texttual search box is empty.
         */
        searchIfEmpty: {
            check: "Boolean",
            init: false
        }
    },

    construct: function (constViewSpec, useColumns, overrideColumsMeta) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
        if (Array.isArray(useColumns)) {
            if (useColumns.indexOf("path") !== -1) {
                constViewSpec = constViewSpec || {};
                constViewSpec["includePath"] = true;
            }
        }
        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", this.refresh, this);
        sf.addListener("input", this.refresh, this);
        sf.addListener("keypress", function (ev) {
            if ("Down" === ev.getKeyIdentifier()) {
                this.__table.handleFocus();
            }
        }, this);
        this.addListener("appear", function () {
            sf.focus();
        });
        this.__table = new ncms.pgs.PagesTable(useColumns, overrideColumsMeta);
        this.__table.getSelectionModel().addListener("changeSelection", function () {
            var page = this.__table.getSelectedPage();
            this.fireDataEvent("itemSelected", page ? page : null);
        }, this);

        this._add(this.__sf);
        this._add(this.__table, {flex: 1});

        this.__table.setConstViewSpec(constViewSpec, true);
    },

    members: {
        __sf: null,
        __table: null,

        getTable: function () {
            return this.__table;
        },

        getSelectedPage: function () {
            return this.__table.getSelectedPage();
        },

        setViewSpec: function (vspec) {
            this.__table.setViewSpec(vspec);
        },

        updateViewSpec: function (vspec) {
            this.__table.updateViewSpec(vspec);
        },

        refresh: function (force) {
            var val = this.__sf.getValue();
            if (!force && !this.getSearchIfEmpty() && sm.lang.String.isEmpty(val)) {
                this.resetSelection();
                this.__table.cleanup();
                return;
            }
            this.updateViewSpec({name: val || ""});
        },

        resetSelection: function() {
            this.__table.resetSelection();
        },

        //overridden
        _applyEnabled: function (value, old) {
            this.base(arguments, value, old);
            this.__sf.setEnabled(value);
            this.__table.setEnabled(value);
        }
    },

    destruct: function () {
        this.__sf = null;
        this.__table = null;
    }
});
