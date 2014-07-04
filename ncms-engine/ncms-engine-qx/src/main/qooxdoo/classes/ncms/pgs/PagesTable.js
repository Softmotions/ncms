/**
 * Pages plain table
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.pgs.PagesTable", {
    extend : sm.table.Table,

    construct : function(useColumns) {
        var tm = new sm.model.RemoteVirtualTableModel({
            "label" : this.tr("Name")
        }).set({
                    "useColumns" : useColumns || ["label"],
                    "rowdataUrl" : ncms.Application.ACT.getUrl("pages.search"),
                    "rowcountUrl" : ncms.Application.ACT.getUrl("pages.search.count")
                });

        var custom = {
            tableColumnModel : function(obj) {
                return new qx.ui.table.columnmodel.Resize(obj);
            }
        };

        this.base(arguments, tm, custom);
    },

    members : {
        getSelectedPageInd : function() {
            return this.getSelectionModel().getAnchorSelectionIndex();
        },

        getSelectedPage : function() {
            var sind = this.getSelectedPageInd();
            return sind != -1 ? this.getTableModel().getRowData(sind) : null;
        },

        getSelectedPages : function() {
            var me = this;
            var pages = [];
            this.getSelectionModel().iterateSelection(function(ind) {
                pages.push(me.getTableModel().getRowData(ind));
            });
            return pages;
        },

        cleanup : function() {
            this.getTableModel().cleanup();
        }
    },

    destruct : function() {
    }
});