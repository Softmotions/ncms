/**
 * Base types selector dialog
 */
qx.Class.define("ncms.cc.TypesSelectorDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * Data:
         * [type, filter_class]
         */
        "completed": "qx.event.type.Data"
    },

    construct: function (caption, typeClassIteratorFn) {
        qx.core.Assert.assertFunction(typeClassIteratorFn);
        this.__iteratorFn = typeClassIteratorFn;
        this.base(arguments, caption || this.tr("Select a type"));
        this.setLayout(new qx.ui.layout.VBox());
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function () {
            this.__search(null);
        }, this);
        sf.addListener("input", function (ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("keypress", this.__searchKeypress, this);
        this.add(sf);

        this.__items = this.__createItems();
        this.__table = this.__createTable();
        this.add(this.__table, {flex: 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.setEnabled(false);
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);

        cmd = this.createCommand("Enter");
        cmd.addListener("execute", this.__ok, this);

        this.addListenerOnce("resize", this.center, this);
        this.addListenerOnce("appear", function () {
            sf.focus();
        });
    },

    members: {

        __items: null,

        __okBt: null,

        __sf: null,

        __table: null,

        __iteratorFn: null,

        __ok: function () {
            var clazz = this.__table.getSelectedRowData();
            var row = this.__table.getSelectedRowData2();
            if (clazz == null || row == null) {
                return;
            }
            this.fireDataEvent("completed", [row[1], clazz]);
        },

        __search: function (text) {
            if (text == null) {
                this.__setTableData(null, this.__items);
                return;
            }
            text = text.toLocaleLowerCase();
            var items = this.__items.filter(function (el) {
                var desc = (el[0][0] == null) ? "" : el[0][0].toLocaleLowerCase();
                var type = (el[0][1] == null) ? "" : el[0][1].toLocaleLowerCase();
                return ((desc.indexOf(text) !== -1) || (type.indexOf(text) === 0));
            });
            this.__setTableData(null, items);
        },

        __searchKeypress: function (ev) {
            if ("Down" === ev.getKeyIdentifier()) {
                this.__table.handleFocus();
            }
        },

        __createItems: function () {
            var items = [];
            this.__iteratorFn(
                function (type, clazz) {
                    items.push([
                        [(clazz.getDescription() || ""), type, (clazz.classname || clazz.toString())],
                        clazz
                    ]);
                }
            );
            items.sort(function (o1, o2) {
                var d1 = o1[0][0];
                var d2 = o2[0][0];
                return (d1 > d2) ? 1 : (d1 < d2 ? -1 : 0);
            });
            return items;
        },

        __createTable: function () {
            var tm = new sm.model.JsonTableModel();
            this.__setTableData(tm, this.__items);
            var table = new sm.table.Table(tm, tm.getCustom());
            table.addListener("cellDbltap", this.__ok, this);
            table.getSelectionModel().addListener("changeSelection", this.__syncState, this);
            table.set({
                showCellFocusIndicator: false,
                statusBarVisible: true,
                focusCellOnPointerMove: true,
                height: 150,
                allowGrowY: true
            });
            return table;
        },

        __syncState: function () {
            this.__okBt.setEnabled(!this.__table.getSelectionModel().isSelectionEmpty());
        },

        __setTableData: function (tm, items) {
            if (tm == null) {
                tm = this.__table.getTableModel();
            }
            items = items || [];
            tm.setJsonData({
                "columns": [
                    {
                        "title": this.tr("Description").toString(),
                        "id": "description",
                        "sortable": true,
                        "width": "2*"
                    },
                    {
                        "title": this.tr("Type").toString(),
                        "id": "type",
                        "sortable": true,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Class").toString(),
                        "id": "class",
                        "sortable": true,
                        "width": "1*",
                        "visible": false
                    }
                ],
                "items": items
            });
        },

        __dispose: function () {
            this.__items = null;
            this.__sf = null;
            this.__table = null;
            this.__okBt = null;
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this.__dispose();
    }

});