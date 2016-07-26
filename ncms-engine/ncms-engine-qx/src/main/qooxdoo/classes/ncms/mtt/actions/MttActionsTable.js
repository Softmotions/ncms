/**
 * Mtt actions attached to rules.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 */
qx.Class.define("ncms.mtt.actions.MttActionsTable", {
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
        "actionsChanged": "qx.event.type.Event"
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

    construct: function (title) {
        this.__title = title;
        this.base(arguments);
        this.set({allowGrowX: true, allowGrowY: true});
        this._reload([]);
    },

    members: {

        __title: null,

        __delBt: null,

        __upBt: null,

        __downBt: null,

        reload: function () {
            var rid = this.getRuleId();
            this.__applyRuleId(rid);
        },

        __applyRuleId: function (id) {
            var items = [];
            if (id == null) {
                this._reload(items);
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.actions.select", {id: id}),
                "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                var freg = ncms.mtt.actions.MttActionsRegistry;
                data.forEach(function (it) {
                    var aclazz = freg.findMttActionClassForType(it["type"]);
                    if (aclazz) {
                        if ((typeof it["spec"] === "string") && it["spec"].length) {
                            it["spec"] = JSON.parse(it["spec"]);
                        } else {
                            it["spec"] = {};
                        }
                        items.push(
                            //todo
                            [["", it["type"], aclazz.specForHuman(it["spec"]), it["description"]], it]
                        );
                    }
                });
                this._reload(items)
            }, this);
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
                        "width": 80
                    },
                    {
                        "title": this.tr("Specification").toString(),
                        "id": "spec",
                        "sortable": false,
                        "width": "2*"
                    },
                    {
                        "title": this.tr("Description").toString(),
                        "id": "description",
                        "sortable": false,
                        "visible": true,
                        "width": "1*"
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

            if (this.__title) {
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
                toolbar.add(new qx.ui.basic.Label(this.__title).set({font: "bold", alignY: "middle"}));
            }

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
            var rr = new sm.table.renderer.CustomRowRenderer();
            var colorm = qx.theme.manager.Color.getInstance();
            rr.setBgColorInterceptor(qx.lang.Function.bind(function (rowInfo) {
                var rdata = rowInfo.rowData.rowData;
                if (!rdata["enabled"]) {
                    return colorm.resolve("table-row-gray");
                } else {
                    return colorm.resolve("background");
                }
            }, this));
            table.setDataRowRenderer(rr);
            table.addListener("cellDbltap", this.__editAction, this);
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

                bt = new qx.ui.menu.Button(this.tr("Edit"));
                bt.addListenerOnce("execute", this.__editAction, this);
                menu.add(bt);

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
            var dlg = new ncms.mtt.actions.MttActionDlg(this.tr("New action"), {
                ruleId: this.getRuleId(),
                enabled: true
            });
            dlg.addListenerOnce("completed", function () {
                dlg.close();
                this.reload();
            }, this);
            dlg.open();
        },

        __removeAction: function () {
            var rd = this.getSelectedRowData();
            qx.core.Assert.assertNotNull(rd);
            ncms.Application.confirm(this.tr("Are you sure to remove action: %1", rd["type"]), function (yes) {
                if (!yes) {
                    return;
                }
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("mtt.action.delete", {id: rd["id"]}), "DELETE");
                req.send(function () {
                    this.removeSelected();
                }, this);
            }, this);
        },

        __editAction: function () {
            var rd = this.getSelectedRowData();
            qx.core.Assert.assertNotNull(rd);
            var dlg = new ncms.mtt.actions.MttActionDlg(this.tr("Edit action: %1", rd["type"]), rd);
            dlg.addListenerOnce("completed", function (ev) {
                dlg.close();
                this.reload();
            }, this);
            dlg.open();
        },

        __onMoveUp: function () {
            this.__move(this.getSelectedRowIndex(), 1);
        },

        __onMoveDown: function () {
            this.__move(this.getSelectedRowIndex(), -1);
        },

        __move: function (ind, dir) {
            //todo
            console.log('Move ind=' + ind + ' dir=' + dir);
        }
    },

    destruct: function () {
        this.__title = null;
        this.__delBt = null;
        this.__upBt = null;
        this.__downBt = null;
    }
});

