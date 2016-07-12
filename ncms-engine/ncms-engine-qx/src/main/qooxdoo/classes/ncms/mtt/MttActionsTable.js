/**
 * Mtt actions attached to rules.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 */
qx.Class.define("ncms.mtt.MttActionsTable", {
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

        __upBt: null,

        __downBt: null,

        __applyRuleId: function (id) {
            var items = [];

            //todo load actions

            this._reload(items)
        },

        //overriden
        _setJsonTableData: function (tm, items) {
            var data = {
                "columns": [
                    {
                        // Action probability
                        "title": this.tr("%").toString(),
                        "id": "prob",
                        "sortable": false,
                        "width": 30
                    },
                    {
                        "title": this.tr("Type").toString(),
                        "id": "type",
                        "sortable": false,
                        "width": 100
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
            bt.setToolTipText(this.tr("New action"));
            bt.addListener("execute", this.__newAction, this);
            part.add(bt);

            bt = this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png").set({enabled: false});
            bt.setToolTipText(this.tr("Remove rule action"));
            bt.addListener("execute", this.__removeAction, this);
            part.add(bt);

            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});

            part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            this.__upBt = this._createButton(null, "ncms/icon/16/misc/arrow_up.png",
                this.__onMoveUp, this);
            this.__upBt.setToolTipText(this.tr("Move up"));
            part.add(this.__upBt);

            this.__downBt = this._createButton(null, "ncms/icon/16/misc/arrow_down.png",
                this.__onMoveDown, this);
            this.__downBt.setToolTipText(this.tr("Move down"));
            part.add(this.__downBt);

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
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt = new qx.ui.menu.Button(this.tr("New action"));
            bt.addListenerOnce("execute", this.__newAction, this);
            menu.add(bt);
            if (rd != null) {
                if (this.__upBt.getEnabled()) {
                    bt = new qx.ui.menu.Button(this.tr("Move up"));
                    bt.addListenerOnce("execute", this.__onMoveUp, this);
                    menu.add(bt);
                }
                if (this.__downBt.getEnabled()) {
                    bt = new qx.ui.menu.Button(this.tr("Move down"));
                    bt.addListenerOnce("execute", this.__onMoveDown, this);
                    menu.add(bt);
                }
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__removeAction, this);
                menu.add(bt);
            }
        },

        __syncState: function () {
            var ri = this.getSelectedRowIndex();
            this.__delBt.setEnabled(ri != null && ri !== -1);
            if (ri != null && ri !== -1) {
                var rc = this.getRowCount();
                this.__upBt.setEnabled(ri > 0);
                this.__downBt.setEnabled(ri < rc - 1);
            } else {
                this.__upBt.setEnabled(false);
                this.__downBt.setEnabled(false);
            }
        },

        __newAction: function () {
            console.log('New action!');
        },

        __removeAction: function () {
            console.log('Remove action!');
        },

        __onMoveUp: function () {
            this.__move(this.getSelectedRowIndex(), 1);
        },

        __onMoveDown: function () {
            this.__move(this.getSelectedRowIndex(), -1);
        },

        __move: function (ind, dir) {

        }
    },

    destruct: function () {
        this.__spec = null;
        this.__delBt = null;
        this.__upBt = null;
        this.__downBt = null;
    }
});

