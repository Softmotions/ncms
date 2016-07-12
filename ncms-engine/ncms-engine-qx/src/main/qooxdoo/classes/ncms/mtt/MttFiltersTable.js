/**
 * Mtt filters attached to rules.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 */
qx.Class.define("ncms.mtt.MttFiltersTable", {
    extend: sm.table.ToolbarLocalTable,
    implement: [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include: [
        sm.ui.form.MStringForm,
        sm.table.MTableMutator
    ],


    events: {
        "filterChanged": "qx.event.type.Event"
    },

    properties: {

        /**
         * Rule ID to sync with
         */
        "ruleId": {
            apply: "__applyRuleId",
            nullable: true,
            check: "Number"
        }
    },

    construct: function () {
        this.base(arguments);
        this.set({allowGrowX: true, allowGrowY: true});
        this._reload([]);
    },

    members: {

        __delBt: null,

        __applyRuleId: function (id) {
            var items = [];

            //todo load filters

            this._reload(items)
        },

        //overriden
        _setJsonTableData: function (tm, items) {
            var data = {
                "columns": [
                    {
                        "title": this.tr("Type").toString(),
                        "id": "type",
                        "sortable": false,
                        "width": 90
                    },
                    {
                        "title": this.tr("Specification").toString(),
                        "id": "spec",
                        "sortable": false,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Description").toString(),
                        "id": "description",
                        "sortable": false,
                        "visible": false,
                        "width": "2*"
                    }
                ],
                "items": items
            };
            tm.setJsonData(data);
            this.__syncState();
        },

        //overriden
        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/actions/add.png");
            bt.setToolTipText(this.tr("New filter"));
            bt.addListener("execute", this.__newFilter, this);
            part.add(bt);

            bt = this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png").set({enabled: false});
            bt.setToolTipText(this.tr("Remove rule filter"));
            bt.addListener("execute", this.__removeFilter, this);
            part.add(bt);

            return toolbar;
        },

        _createButton: function (label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overriden
        _createTable: function (tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.getSelectionModel().addListener("changeSelection", this.__syncState, this);
            table.set({
                showCellFocusIndicator: false,
                statusBarVisible: false,
                focusCellOnPointerMove: false
            });
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            return table;
        },

        __beforeContextmenuOpen: function (ev) {
            console.log('!!!!');

            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt = new qx.ui.menu.Button(this.tr("New filter"));
            bt.addListenerOnce("execute", this.__newFilter, this);
            menu.add(bt);
            if (rd != null) {
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__removeFilter, this);
                menu.add(bt);
            }
        },

        __syncState: function () {
            var ri = this.getSelectedRowIndex();
            this.__delBt.setEnabled(ri != null && ri !== -1);
        },

        __newFilter: function () {
            console.log('New filter!');
        },

        __removeFilter: function () {
            console.log('Remove filter!');
        }
    },

    destruct: function () {
        this.__spec = null;
        this.__delBt = null;
    }

});

