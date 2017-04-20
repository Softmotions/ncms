/**
 * Virtual table of mtt rules.
 *
 * //asset(ncms/icon/16/mtt/*)
 */
qx.Class.define("ncms.mtt.MttRulesTable", {
    extend: sm.table.ToolbarTable,

    /**
     * @param useColumns {Array?} Array of used columns
     * @param smodel {qx.ui.table.selection.Model?} Optional selection model
     * @param toolbarInitializerFn {function?} Toolbar initializer function
     */
    construct: function (useColumns, smodel, toolbarInitializerFn) {
        this.__useColumns = useColumns || ["name"];
        this.__smodel = smodel;
        this.__toolbarInitializerFn = toolbarInitializerFn;
        this.__cmeta = {
            name: {
                title: this.tr("Name").toString(),
                width: "1*"
            }
        };
        this.base(arguments);
        this._reload();
        ncms.Events.getInstance().addListener("mttRulePropsChanged", this.__onMttRulePropsChanged, this);
    },

    members: {

        __toolbarInitializerFn: null,

        __smodel: null,

        __cmeta: null,

        __useColumns: null,

        /**
         * @param tableModel {sm.model.RemoteVirtualTableModel}
         */
        _createTable: function (tableModel) {
            var useColumns = this.__useColumns;
            var cmeta = this.__cmeta;
            var custom = {
                tableColumnModel: function () {
                    return new sm.model.JsonTableColumnModel(
                        useColumns.map(function (cname) {
                            return cmeta[cname];
                        }));
                }
            };
            var rr = new sm.table.renderer.CustomRowRenderer();
            var colorm = qx.theme.manager.Color.getInstance();
            rr.setBgColorInterceptor(qx.lang.Function.bind(function (rowInfo) {
                var rdata = rowInfo.rowData;
                if (rdata != null && !rdata["enabled"]) {
                    return colorm.resolve("table-row-gray");
                } else {
                    return colorm.resolve("background");
                }
            }, this));
            var table = new sm.table.Table(tableModel, custom).set({
                statusBarVisible: true,
                showCellFocusIndicator: false,
                focusCellOnPointerMove: false,
                columnVisibilityButtonVisible: false,
                dataRowRenderer: rr
            });
            if (this.__smodel) {
                table.setSelectionModel(this.__smodel);
            }
            tableModel.setTable(table);
            return table;
        },

        // override
        _createTableModel: function () {
            var cmeta = this.__cmeta;
            return new sm.model.RemoteVirtualTableModel(cmeta, this.__useColumns).set({
                "rowdataUrl": ncms.Application.ACT.getUrl("mtt.rules.select"),
                "rowcountUrl": ncms.Application.ACT.getUrl("mtt.rules.select.count")
            });
        },

        _createToolbarItems: function (toolbar) {
            if (typeof this.__toolbarInitializerFn === "function") {
                return this.__toolbarInitializerFn(toolbar);
            }
            return toolbar;
        },

        resetSelection: function() {
            this._table.resetSelection();
        },

        getSelectedRuleInd: function () {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedRule: function () {
            var sind = this.getSelectedRuleInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedRules: function () {
            var me = this;
            var rules = [];
            this.getSelectionModel().iterateSelection(function (ind) {
                rules.push(me.getTableModel().getRowData(ind));
            });
            return rules;
        },

        cleanup: function () {
            this.getTableModel().cleanup();
        },

        __onMttRulePropsChanged: function (ev) {
            //todo
        }
    },

    destruct: function () {
        this.__cmeta = null;
        this.__useColumns = null;
        this.__smodel = null;
        this.__toolbarInitializerFn = null;
        ncms.Events.getInstance().removeListener("mttRulePropsChanged", this.__onMttRulePropsChanged, this);
    }
});