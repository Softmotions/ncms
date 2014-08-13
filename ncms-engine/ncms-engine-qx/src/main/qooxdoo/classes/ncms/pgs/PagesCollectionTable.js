/**
 * Pages collection table
 */
qx.Class.define("ncms.pgs.PagesCollectionTable", {
    extend : sm.table.ToolbarLocalTable,

    statics : {
    },

    events : {
    },

    properties : {
    },

    construct : function(options) {
        this.__options = options || {};
        this.base(arguments);
        this._reload([]);
    },

    members : {

        __options : null,

        __onAddPage : function() {
            var dlg = new ncms.pgs.PagesSelectorDlg(null, false,
                    {accessAll : this.__options["accessAll"]});
            dlg.addListener("completed", function(ev) {
                var data = ev.getData();
                qx.log.Logger.info("onAddPage data=" + JSON.stringify(data));
                dlg.close();
            });
            dlg.open();
        },

        __onRemovePage : function() {
            qx.log.Logger.info("on remove");
        },

        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/actions/add.png");
            bt.setToolTipText(this.tr("Add page"));
            bt.addListener("execute", this.__onAddPage, this);
            part.add(bt);


            bt = this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png").set({enabled : false});
            bt.setToolTipText(this.tr("Remove page"));
            bt.addListener("execute", this.__onRemovePage, this);
            part.add(bt);

            return toolbar;
        },

        _createTable : function(tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.getSelectionModel().addListener("changeSelection", this._syncState, this);
            table.set({
                showCellFocusIndicator : false,
                statusBarVisible : false,
                focusCellOnPointerMove : false});
            return table;
        },

        _createButton : function(label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance" : "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overriden
        _setJsonTableData : function(tm, items) {
            var data = {
                "columns" : [
                    {
                        "title" : this.tr("Name").toString(),
                        "id" : "name",
                        "sortable" : true,
                        "width" : "1*"
                    },

                    {
                        "title" : this.tr("Path").toString(),
                        "id" : "path",
                        "sortable" : true,
                        "width" : "1*"
                    }

                ],
                "items" : items
            };
            tm.setJsonData(data);
            this._syncState();
        },

        _syncState : function() {
            var ri = this.getSelectedRowIndex();
            this.__delBt.setEnabled(ri != null && ri !== -1);
        }
    },

    destruct : function() {
        this.__delBt = null;
    }
});
