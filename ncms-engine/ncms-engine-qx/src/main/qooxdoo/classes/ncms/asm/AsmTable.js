/**
 * Virtual table of assemblies.
 *
 * @asset(ncms/icon/16/asm/*)
 */
qx.Class.define("ncms.asm.AsmTable", {
    extend: sm.table.Table,

    construct: function (useColumns) {
        var cmeta = {
            icon: {
                title: "",
                type: "image",
                width: 26
            },
            name: {
                title: this.tr("Name").toString(),
                width: "2*"
            },
            description: {
                title: this.tr("Description").toString(),
                width: "3*"
            },
            type: {
                title: this.tr("Type").toString(),
                width: "1*",
                visible: false
            }
        };
        useColumns = useColumns || ["icon", "name", "description", "type"];
        var tm = new sm.model.RemoteVirtualTableModel(cmeta, null, this).set({
            "useColumns": useColumns,
            "rowdataUrl": ncms.Application.ACT.getUrl("asms.select"),
            "rowcountUrl": ncms.Application.ACT.getUrl("asms.select.count")
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

        ncms.Events.getInstance().addListener("asmPropsChanged", this.__onAsmPropsChanged, this);
    },

    members: {

        getSelectedAsmInd: function () {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedAsm: function () {
            var sind = this.getSelectedAsmInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedAsms: function () {
            var me = this;
            var asms = [];
            this.getSelectionModel().iterateSelection(function (ind) {
                asms.push(me.getTableModel().getRowData(ind));
            });
            return asms;
        },

        cleanup: function () {
            this.getTableModel().cleanup();
        },

        __onAsmPropsChanged: function (ev) {
            var data = ev.getData();
            var id = data["id"];
            this.getTableModel().updateCachedRows(function (ind, rowdata) {
                if (rowdata["id"] === id) {
                    rowdata["name"] = data["name"];
                    rowdata["description"] = data["description"];
                    var tmode = data["templateMode"];
                    var type = data["type"];
                    if ("none" != tmode) {
                        rowdata["icon"] = "ncms/icon/16/asm/template.png"
                    } else if ("news.page" == type) {
                        rowdata["icon"] = "ncms/icon/16/asm/news.png";
                    } else if (!sm.lang.String.isEmpty(type)) {
                        rowdata["icon"] = "ncms/icon/16/asm/page.png";
                    } else {
                        rowdata["icon"] = "ncms/icon/16/asm/other.png";
                    }
                    return rowdata;
                }
            }, this);
        }
    },

    destruct: function () {
        ncms.Events.getInstance().removeListener("asmPropsChanged", this.__onAsmPropsChanged, this);
    }
});