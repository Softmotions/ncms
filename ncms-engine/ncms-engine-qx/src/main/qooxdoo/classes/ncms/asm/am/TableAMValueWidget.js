/**
 * Table input control.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 */
qx.Class.define("ncms.asm.am.TableAMValueWidget", {
    extend: sm.table.ToolbarLocalTable,

    implement: [
        qx.ui.form.IModel,
        qx.ui.form.IForm,
        qx.ui.form.IStringForm,
        ncms.asm.am.IValueWidget
    ],

    include: [
        qx.ui.form.MForm,
        sm.table.MTableMutator,
        ncms.asm.am.MValueWidget,
        ncms.cc.MCommands,
        qx.core.MAssert
    ],

    events: {
        /** Fired when the model data changes */
        "changeModel": "qx.event.type.Data"
    },

    properties: {

        value: {
            check: "String",
            nullable: true,
            event: "changeValue",
            apply: "__applyValue"
        }
    },

    construct: function (model) {
        this.__broadcaster = sm.event.Broadcaster.create({
            "up": false,
            "down": false,
            "sel": false
        });
        this.__historyCols = [];

        this.base(arguments);

        this.setHeight(230);
        if (model != null) {
            this.setModel(model);
        }

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onAdd, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onRemoveConfirm, this);

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
    },

    members: {

        __broadcaster: null,

        __spCols: null,

        __inModel: false,

        __historyCols: null,

        valueAsJSON: function () {
            return this.getModel() || [];
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

            el = this.__spCols = new qx.ui.form.Spinner(1, 2, 1000);
            el.setAllowGrowY(true);
            el.getChildControl("textfield").setAllowGrowY(true);
            el.setToolTipText(this.tr("Number of table columns"));
            el.addListener("changeValue", this.__onChangeColumns, this);
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

        /**
         * @param tableModel {sm.model.JsonTableModel}
         */
        _createTable: function (tableModel) {
            var t = new sm.table.Table(tableModel, tableModel.getCustom())
            .set({"statusBarVisible": false});
            t.getSelectionModel().setSelectionMode(qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION);

            // Setup first row as bold header cell renderer
            var cm = t.getTableColumnModel();
            var cr = new ncms.cc.table.TableHeaderCRenderer();
            var nCols = cm.getOverallColumnCount();
            for (var i = 0; i < nCols; ++i) {
                t.getTableColumnModel().setDataCellRenderer(i, cr);
            }

            // Listeners
            t.getSelectionModel().addListener("changeSelection", this.__syncState, this);
            t.addListener("dataEdited", this.__dataEdited, this);

            // Shortcuts
            this._registerCommandFocusWidget(t);
            return t;
        },

        _createTableModel: function () {
            return new sm.model.JsonTableModel();
        },

        __applyValue: function (val) {
            if (val != null) {
                this.setModel(JSON.parse(val));
            } else {
                this.setModel(null);
            }
        },

        setModel: function (val) {
            try {
                this.__inModel = true;
                this._setModel(val);
            } finally {
                this.__inModel = false;
            }
        },

        _setModel: function (val) {
            this.assertTrue(val == null || Array.isArray(val));
            var cols = 0;
            if (val.length < 1 || val == null) {
                cols = 2;
                val = [new Array(cols).fill("")];
            }
            val = val.map(function (el) {
                if (el.length > cols) {
                    cols = el.length;
                }
                return [el, {}];
            });
            this.__spCols.setValue(cols);
            var columns = [];
            for (var i = 1; i <= cols; ++i) {
                columns.push({
                    "title": i.toString(),
                    "id": i.toString(),
                    "width": i == 1 ? "1*" : "3*",
                    "minWidth": i == 1 ? 70 : null,
                    "sortable": false,
                    "editable": true
                })
            }
            var data = {
                "columns": columns,
                "items": val
            };
            if (this._table != null) {
                if (this._table.isEditing()) {
                    this._table.stopEditing();
                }
                var cm = this._table.getTableColumnModel();
                if (cm.getOverallColumnCount() != columns.length) {
                    this._table.destroy();
                    this._table = null;
                }
            }
            this._reload(data);
            this.fireDataEvent("changeModel", this.getModel());
        },

        getModel: function () {
            if (this._table == null) {
                return null;
            }
            return this._table.getTableModel().getData().map(function (el) {
                return [].concat(el);
            });
        },

        resetModel: function () {
            this.setModel(null);
        },

        _setJsonTableData: function (tm, data) {
            tm.setJsonData(data);
        },

        __onAdd: function () {
            var cols = this.__spCols.getValue();
            this.addRow({}, new Array(cols).fill(""));
            var rc = this.getRowCount();
            if (rc > 0) {
                this.getTable().selectSingleRow(rc - 1);
            }
            this.fireEvent("modified");
        },


        __onRemoveConfirm: function () {
            console.log("!!!C");
            ncms.Application.confirm(this.tr("Are you sure to remove the selected rows?"), function (yes) {
                if (!yes) return;
                this.__onRemove();
            }, this);
        },

        __onRemove: function () {
            this.removeSelected();
            this.fireEvent("modified");
        },

        __onChangeColumns: function (ev) {
            if (this.__inModel) {
                return;
            }
            var nc = this.__spCols.getValue();
            var cc = this._table.getTableColumnModel().getOverallColumnCount();
            var m, i;

            if (nc < cc) {
                i = 0;
                m = this._table.getTableModel().getData().map(function (el) {
                    el = [].concat(el);
                    var removed = el.splice(-1, 1);
                    if (removed.length > 0) {
                        if (this.__historyCols[nc] == null) {
                            this.__historyCols[nc] = [];
                        }
                        this.__historyCols[nc][i++] = removed[0];
                    }
                    return el;
                }, this);
                this.setModel(m);

            } else if (nc > cc) {
                i = 0;
                m = this._table.getTableModel().getData().map(function (el) {
                    var add = "";
                    if (this.__historyCols[nc - 1]) {
                        add = this.__historyCols[nc - 1][i] || "";
                    }
                    i++;
                    return el.concat(add);
                }, this);
                this.setModel(m);
            }

            this.fireEvent("modified");
        },

        __onMoveUp: function () {
            this.moveRowByIndex(this.getSelectedRowIndex(), -1, true);
            this.fireDataEvent("changeModel", this.getModel());
            this.fireEvent("modified");
        },

        __onMoveDown: function () {
            this.moveRowByIndex(this.getSelectedRowIndex(), 1, true);
            this.fireDataEvent("changeModel", this.getModel());
            this.fireEvent("modified");
        },

        __dataEdited: function () {
            this.fireDataEvent("changeModel", this.getModel());
            this.fireEvent("modified");
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

        __beforeContextmenuOpen : function(ev) {
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("New row"));
            bt.addListenerOnce("execute", this.__onAdd, this);
            menu.add(bt);

            if (rd != null) {
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemove, this);
                menu.add(bt);
            }
        }
    },

    destruct: function () {
        this.__spCols = null;
        this.__historyCols = [];
        if (this.__broadcaster) {
            this.__broadcaster.destruct();
            this.__broadcaster = null;
        }
    }
});