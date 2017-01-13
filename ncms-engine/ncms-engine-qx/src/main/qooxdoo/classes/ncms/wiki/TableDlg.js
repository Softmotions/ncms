/**
 * Insert table dialog.
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.wiki.TableDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * Fired if insert table button clicked
         * Table model and optional css classes
         *
         * data: [{@link qx.ui.table.model.Simple}, cssClasses]
         */
        "completed": "qx.event.type.Data"
    },

    properties: {},

    construct: function (opts) {
        opts = opts || {};
        this.base(arguments, this.tr("Insert table"));
        this.setLayout(new qx.ui.layout.Dock(5, 5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 520,
            height: 460
        });

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", function () {
            this.close();
        }, this);

        var header = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({alignY: "middle"}));
        this.__spCols = new qx.ui.form.Spinner(1, 2, 10);
        this.__spCols.addListener("changeValue", this._updateTable, this);
        this.__spRows = new qx.ui.form.Spinner(1, 2, 100);
        this.__spRows.addListener("changeValue", this._updateTable, this);

        header.add(new qx.ui.basic.Label(this.tr("Columns")));
        header.add(this.__spCols);
        header.add(new qx.ui.basic.Label(this.tr("Rows")));
        header.add(this.__spRows);
        this.add(header, {edge: "north"});

        this._updateTable();

        var footer = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({alignX: "right"}));
        var ok = new qx.ui.form.Button(this.tr("Insert table"));
        ok.addListener("execute", function (ev) {
            if (this.__table.isEditing()) {
                this.__table.stopEditing();
            }
            var data = [this.__table.getTableModel(), cssClasses.getValue()];
            this.fireDataEvent("completed", data);
        }, this);
        var cancel = new qx.ui.form.Button(this.tr("Cancel"));
        cancel.addListener("execute", function (ev) {
            this.close();
        }, this);

        footer.add(ok);
        footer.add(cancel);
        this.add(footer, {edge: "south"});

        var cssClasses = new qx.ui.form.TextField();
        if (!opts["noClasses"]) {
            var cont = new qx.ui.container.Composite(new qx.ui.layout.HBox(4).set({alignY: "middle"}));
            cont.add(new qx.ui.basic.Label(this.tr("CSS classes:")));
            cont.add(cssClasses);
            this.add(cont, {edge: "south"});
        }
        this.addListenerOnce("resize", function () {
            this.center();
        }, this);
    },

    members: {
        __spCols: null,

        __spRows: null,

        __table: null,

        open: function () {
            this.base(arguments);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        },

        _updateTable: function () {
            var oldTable = this.__table;
            var oldTm = oldTable ? oldTable.getTableModel() : null;

            if (oldTable && oldTable.isEditing()) {
                oldTable.stopEditing();
            }

            var tm = new qx.ui.table.model.Simple();
            var cols = [];
            var nCols = this.__spCols.getValue();
            for (var i = 0; i < nCols; ++i) {
                cols.push(String(i + 1));
            }
            tm.setColumns(cols);
            for (var i = 0; i < nCols; ++i) {
                tm.setColumnEditable(i, true);

            }
            var rowData = [];
            var nRows = this.__spRows.getValue();
            for (var i = 0; i < nRows; ++i) {
                var oldRow = oldTm ? oldTm.getRowData(i) : null;
                var row = [];
                for (var j = 0; j < nCols; ++j) {
                    row[j] = ((oldRow && oldRow[j] != null) ? oldRow[j] : "");
                }
                rowData.push(row);
            }
            tm.setData(rowData);

            if (oldTm == null || oldTm.getColumnCount() != cols.length) { //new or changed columns count
                //Replace table if cells changed
                this.__table = new qx.ui.table.Table(tm);
                if (oldTable) {
                    this.remove(oldTable);
                }
                this.add(this.__table, {edge: "center"});
                if (oldTable) {
                    oldTable.dispose();
                }
            } else {
                this.__table.setTableModel(tm);
            }

            var hrend = new ncms.cc.table.TableHeaderCRenderer();
            for (var i = 0; i < nCols; ++i) {
                tm.setColumnSortable(i, false);
                this.__table.getTableColumnModel().setDataCellRenderer(i, hrend);
            }
        }
    },

    destruct: function () {
        this.__spCols = null
        this.__spRows = null;
        this.__table = null;
    }
});