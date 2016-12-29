qx.Class.define("ncms.pgs.referrers.PageReferrersTable", {
        extend: sm.table.Table,

        construct: function (dataUrl, countUrl) {
            var cmeta = {
                icon: {
                    title: "",
                    type: "image",
                    width: 26,
                    sortable: false
                },
                name: {
                    title: this.tr("Name").toString()
                },
                path: {
                    title: this.tr("Path").toString(),
                    sortable: false
                }
            };

            var useColumns = ["icon", "name", "path"];
            var tm = new sm.model.RemoteVirtualTableModel(cmeta).set({
                "useColumns": useColumns,
                "rowdataUrl": dataUrl,
                "rowcountUrl": countUrl
            });

            var custom = {
                tableColumnModel: function () {
                    return new sm.model.JsonTableColumnModel(
                        useColumns.map(function (cname) {
                            return cmeta[cname];
                        }));
                }
            };
            tm.setViewSpec({sortInd: 1});
            this.base(arguments, tm, custom);
        },

        members: {

            setViewSpec: function (spec) {
                this.getTableModel().setViewSpec(spec);
            },

            getViewSpec: function () {
                return this.getTableModel().getViewSpec();
            },

            getSelectedPageInd: function () {
                return this.getSelectionModel().getAnchorSelectionIndex();
            },

            getSelectedPage: function () {
                var i = this.getSelectedPageInd();
                return i != -1 ? this.getTableModel().getRowData(i) : null;
            },

            getRowData: function (rowId) {
                return this.getRowData2(rowId);
            }
        }
    }
);