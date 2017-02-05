/**
 * Select box options table.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 */
qx.Class.define("ncms.asm.am.SelectAMTable", {
    extend: sm.table.ToolbarLocalTable,
    implement: [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include: [
        sm.ui.form.MStringForm,
        sm.table.MTableMutator
    ],

    properties: {

        /**
         * Array of key-value pairs.
         */
        data: {
            check: "Array",
            nullable: true,
            apply: "__applyData"
        },


        /**
         * Item select mode
         */
        checkMode: {
            check: ["single", "multiply"],
            nullable: false,
            init: "single",
            apply: "__applyCheckMode"
        }
    },

    construct: function (data) {
        this.__broadcaster = sm.event.Broadcaster.create({
            "up": false,
            "down": false,
            "sel": false
        });
        this.base(arguments);
        this.set({height: 200});
        this.setData(data || []);
    },

    members: {

        __broadcaster: null,

        _setJsonTableData: function (tm, items) {
            var data = {
                "columns": [
                    {
                        "title": this.tr("Selected").toString(),
                        "id": "checked",
                        "sortable": false,
                        "type": "boolean",
                        "editable": true,
                        "width": 60
                    },
                    {
                        "title": this.tr("Name").toString(),
                        "id": "name",
                        "sortable": false,
                        "editable": true,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Value").toString(),
                        "id": "value",
                        "sortable": false,
                        "editable": true,
                        "width": "1*"
                    }
                ],
                "items": items
            };
            tm.setJsonData(data);
        },

        /**
         * @param tableModel {sm.model.JsonTableModel}
         */
        _createTable: function (tableModel) {
            var table = new sm.table.Table(tableModel, tableModel.getCustom());
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            table.addListener("dataEdited", function (ev) {
                var data = ev.getData();
                if (data.col !== 0 || this.getCheckMode() === "multiply") {
                    return;
                }
                var val = data.value;
                if (val == true) {
                    var rc = this.getTableModel().getRowCount();
                    while (--rc >= 0) {
                        if (rc != data.row && this.getCellValue(rc, 0) === true) {
                            this.setCellValue(rc, 0, false);
                        }
                    }
                }
            }, this);
            table.getSelectionModel()
            .addListener("changeSelection", this.__syncState, this);
            return table;
        },

        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);
            var el = this._createButton(null, "ncms/icon/16/actions/add.png",
                this.__onAdd, this);
            el.setToolTipText(this.tr("Add record"));
            part.add(el);

            el = this._createButton(null, "ncms/icon/16/actions/delete.png",
                this.__onRemove, this);
            el.setToolTipText(this.tr("Remove record"));
            this.__broadcaster.attach(el, "sel", "enabled");
            part.add(el);

            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});

            part = new qx.ui.toolbar.Part()
            .set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            el = this._createButton(null, "ncms/icon/16/misc/arrow_up.png",
                this.__onMoveUp, this);
            this.__broadcaster.attach(el, "up", "enabled");
            part.add(el);

            el = this._createButton(null, "ncms/icon/16/misc/arrow_down.png",
                this.__onMoveDown, this);
            this.__broadcaster.attach(el, "down", "enabled");
            part.add(el);

            return toolbar;
        },

        _createButton: function (label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon)
            .set({"appearance": "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        __beforeContextmenuOpen: function (ev) {
            var rd = this.getSelectedRowData2();
            var menu = ev.getData().getTarget();
            menu.removeAll();
        },

        __applyData: function (data) {
            if (data == null) {
                this._reload([]);
                return;
            }
            var items = [];
            data.forEach(function (el) {
                items.push([
                    [!!el[0], el[1], el[2]],
                    {}
                ]);
            });
            this._reload(items);
        },

        __onAdd: function (ev) {
            this.addRow({}, [false, "", ""]);
        },

        __onRemove: function (ev) {
            var ind = this.getSelectedRowIndex();
            if (ind == -1) {
                return;
            }
            this.removeRowByIndex(ind);
            this._table.resetSelection();
        },

        __onMoveUp: function () {
            this.moveRowByIndex(this.getSelectedRowIndex(), -1, true);
        },

        __onMoveDown: function () {
            this.moveRowByIndex(this.getSelectedRowIndex(), 1, true);
        },

        __applyCheckMode: function (val) {
            if (val == "single") {
                var rc = this.getTableModel().getRowCount();
                while (--rc >= 0) {
                    if (this.getCellValue(rc, 0) == true) {
                        this.setCellValue(rc, 0, false);
                    }
                }
            }
        },

        __syncState: function () {
            var rc = this.getRowCount();
            var rd = this._table.getSelectedRowData2();
            var b = this.__broadcaster;
            b.setSel(rd != null);
            if (rd != null) {
                var rind = this.getSelectedRowIndex();
                b.setUp(rind > 0);
                b.setDown(rind < rc - 1);
            } else {
                b.setUp(false);
                b.setDown(false);
            }
        },

        toJSONValue: function () {
            var rc = this.getTableModel().getRowCount();
            var arr = [];
            for (var i = 0; i < rc; ++i) {
                var rd = this._table.getRowData2(i);
                if (rd != null) {
                    arr.push(rd);
                }
            }
            return arr;
        }
    },

    destruct: function () {
        if (this.__broadcaster) {
            this.__broadcaster.destruct();
            this.__broadcaster = null;
        }
    }
});