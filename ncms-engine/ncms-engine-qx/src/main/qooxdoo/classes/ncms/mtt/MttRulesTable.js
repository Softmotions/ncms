/**
 * Virtual table of mtt rules.
 *
 * @asset(ncms/icon/16/mtt/*)
 */
qx.Class.define("ncms.mtt.MttRulesTable", {
    extend: sm.table.Table,

    construct: function (useColumns) {
        var cmeta = {

            name: {
                title: this.tr("Name").toString(),
                width: "1*"
            }
        };
        useColumns = useColumns || ["name"];
        var tm = new sm.model.RemoteVirtualTableModel(cmeta).set({
            "useColumns": useColumns,
            "rowdataUrl": ncms.Application.ACT.getUrl("mtt.rules.select"),
            "rowcountUrl": ncms.Application.ACT.getUrl("mtt.rules.select.count")
        });

        var custom = {
            tableColumnModel: function () {
                return new sm.model.JsonTableColumnModel(
                    useColumns.map(function (cname) {
                        return cmeta[cname];
                    }));
            }
        };

        this.base(arguments, tm, custom);

        var rr = new sm.table.renderer.CustomRowRenderer();
        var colorm = qx.theme.manager.Color.getInstance();
        rr.setBgColorInterceptor(qx.lang.Function.bind(function (rowInfo) {
            return colorm.resolve("background");
        }, this));
        this.setDataRowRenderer(rr);

        ncms.Events.getInstance().addListener("mttRulePropsChanged", this.__onMttRulePropsChanged, this);
    },

    members: {

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
        ncms.Events.getInstance().removeListener("mttRulePropsChanged", this.__onMttRulePropsChanged, this);
    }
});