/**
 * Virtual table of users.
 */
qx.Class.define("ncms.usr.UsersTable", {
    extend: sm.table.Table,

    construct: function (useColumns) {
        var tm = new sm.model.RemoteVirtualTableModel({
            "name": this.tr("Login"),
            "fullName": this.tr("Name")
        }, null, this).set({
            "useColumns": useColumns || ["name", "fullName"],
            "rowdataUrl": ncms.Application.ACT.getUrl("security.users"),
            "rowcountUrl": ncms.Application.ACT.getUrl("security.users.count")
        });

        var custom = {
            tableColumnModel: function (obj) {
                return new qx.ui.table.columnmodel.Resize(obj);
            }
        };

        this.base(arguments, tm, custom);

        var rr = new sm.table.renderer.CustomRowRenderer();
        var colorm = qx.theme.manager.Color.getInstance();
        rr.setBgColorInterceptor(qx.lang.Function.bind(function (rowInfo) {
            return colorm.resolve("background");
        }, this));
        this.setDataRowRenderer(rr);


        var tcm = this.getTableColumnModel();
        var cInd = tm.getColumnIndexById("name");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "1*");
        }
        cInd = tm.getColumnIndexById("fullName");
        if (cInd != null) {
            tcm.getBehavior().setWidth(cInd, "2*");
        }
        this.set({
            "statusBarVisible": false,
            "showCellFocusIndicator": false,
            "columnVisibilityButtonVisible": false
        });
    },

    members: {

        getSelectedUserInd: function () {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedUser: function () {
            var sind = this.getSelectedUserInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedUsers: function () {
            var me = this;
            var users = [];
            this.getSelectionModel().iterateSelection(function (ind) {
                users.push(me.getTableModel().getRowData(ind));
            });
            return users;
        },

        cleanup: function () {
            this.getTableModel().cleanup();
        }
    },

    destruct: function () {
        //this._disposeObjects("__field_name");
    }
});