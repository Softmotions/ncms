/**
 * Select box options table.
 */
qx.Class.define("ncms.asm.am.SelectAMTable", {
    extend : sm.table.ToolbarLocalTable,
    implement : [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include : [
        sm.ui.form.MStringForm,
        sm.table.MTableMutator
    ],

    properties : {

        /**
         * Array of key-value pairs.
         */
        data : {
            check : "Array",
            nullable : true,
            apply : "__applyData"
        }
    },

    construct : function(data) {
        this.base(arguments);
        this.set({height : 200});
        this.setData(data || []);
    },

    members : {

        _setJsonTableData : function(tm, items) {
            var data = {
                "columns" : [
                    {
                        "title" : this.tr("Name").toString(),
                        "id" : "name",
                        "sortable" : false,
                        "editable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Value").toString(),
                        "id" : "value",
                        "sortable" : false,
                        "editable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Selected").toString(),
                        "id" : "checked",
                        "sortable" : false,
                        "type" : "boolean",
                        "editable" : true,
                        "width" : 60
                    }
                ],
                "items" : items
            };
            tm.setJsonData(data);
        },

        _createTable : function(tableModel) {
            var table = new sm.table.Table(tableModel, tableModel.getCustom());
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            return table;
        },

        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);
            var el = this._createButton(null, "ncms/icon/16/actions/add.png",
                    this.__onAdd, this);
            el.setToolTipText(this.tr("Add record"));
            part.add(el);

            el = this._createButton(null, "ncms/icon/16/actions/delete.png",
                    this.__onRemove, this);
            el.setToolTipText(this.tr("Drop record"));
            part.add(el);

            toolbar.add(new qx.ui.core.Spacer(), {flex : 1});

            part = new qx.ui.toolbar.Part()
                    .set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            el = this._createButton(null, "ncms/icon/16/misc/arrow_up.png",
                    this.__onMoveUp, this);
            part.add(el);

            el = this._createButton(null, "ncms/icon/16/misc/arrow_down.png",
                    this.__onMoveDown, this);
            part.add(el);

            return toolbar;
        },

        _createButton : function(label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance" : "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        __beforeContextmenuOpen : function(ev) {
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();
        },

        __applyData : function(data) {
            //todo
            this._reload([]);
        },

        __onAdd : function(ev) {

        },

        __onRemove : function(ev) {

        },

        __onMoveUp : function(ev) {

        },

        __onMoveDown : function(ev) {

        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});