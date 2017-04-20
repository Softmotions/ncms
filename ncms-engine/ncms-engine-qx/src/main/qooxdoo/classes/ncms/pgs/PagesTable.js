/**
 * Pages plain table.
 *
 * @asset(ncms/icon/16/misc/exclamation.png)
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.pgs.PagesTable", {
    extend: sm.table.Table,

    construct: function (useColumns, overrideMeta) {
        var cmeta = {
            icon: {
                title: "",
                type: "image",
                width: 26,
                sortable: false
            },
            label: {
                title: this.tr("Name").toString()
            },
            path: {
                title: this.tr("Path").toString(),
                sortable: false
            }
        };
        if (overrideMeta) {
            for (var k in overrideMeta) {
                if (cmeta[k] && overrideMeta[k]) {
                    qx.lang.Object.mergeWith(cmeta[k], overrideMeta[k], true);
                }
            }
        }
        useColumns = useColumns || ["icon", "label"];
        var tm = new sm.model.RemoteVirtualTableModel(cmeta, null, this).set({
            "useColumns": useColumns,
            "rowdataUrl": ncms.Application.ACT.getUrl("pages.search"),
            "rowcountUrl": ncms.Application.ACT.getUrl("pages.search.count")
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
        this.set({
            "statusBarVisible": false,
            "showCellFocusIndicator": false,
            "columnVisibilityButtonVisible": false
        })
    },

    members: {

        setViewSpec: function (spec) {
            this.getTableModel().setViewSpec(spec);
        },

        getViewSpec: function () {
            return this.getTableModel().getViewSpec();
        },

        updateViewSpec: function (spec) {
            return this.getTableModel().updateViewSpec(spec);
        },

        setConstViewSpec: function (spec, noupdate) {
            this.getTableModel().setConstViewSpec(spec, noupdate);
        },

        getConstViewSpec: function () {
            return this.getTableModel().getConstViewSpec();
        },

        getSelectedPageInd: function () {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedPage: function () {
            var sind = this.getSelectedPageInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedPages: function () {
            var me = this;
            var pages = [];
            this.getSelectionModel().iterateSelection(function (ind) {
                pages.push(me.getTableModel().getRowData(ind));
            });
            return pages;
        },

        cleanup: function () {
            this.getTableModel().cleanup();
        }
    },

    destruct: function () {
    }
});