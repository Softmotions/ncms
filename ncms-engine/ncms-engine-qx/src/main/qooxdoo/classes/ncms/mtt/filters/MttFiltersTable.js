/**
 * Mtt filters attached to rules.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 */
qx.Class.define("ncms.mtt.filters.MttFiltersTable", {
    extend: sm.table.ToolbarLocalTable,
    implement: [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include: [
        sm.ui.form.MStringForm,
        sm.table.MTableMutator,
        ncms.cc.MCommands
    ],

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

        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__newFilter, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__removeFilter, this);
    },

    members: {

        __title: null,

        __delBt: null,

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
                ncms.Application.ACT.getRestUrl("mtt.filters.select", {id: id}),
                "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                var freg = ncms.mtt.filters.MttFiltersRegistry;
                data.forEach(function (it) {
                    var fclazz = freg.findMttFilterClassForType(it["type"]);
                    if (fclazz) {
                        if ((typeof it["spec"] === "string") && it["spec"].length) {
                            it["spec"] = JSON.parse(it["spec"]);
                        } else {
                            it["spec"] = {};
                        }
                        items.push(
                            [[it["type"], fclazz.specForHuman(it["spec"]), it["description"]], it]
                        );
                    }
                });
                this._reload(items)
            }, this);
        },

        //overridden
        _setJsonTableData: function (tm, items) {
            var data = {
                "columns": [
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

        //overridden
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

            if (this.__title) {
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
                toolbar.add(new qx.ui.basic.Label(this.__title).set({font: "bold", alignY: "middle"}));
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
            }
            return toolbar;
        },

        _createButton: function (label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overridden
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
            table.addListener("cellDbltap", this.__editFilter, this);
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            this._registerCommandFocusWidget(table);
            return table;
        },

        __beforeContextmenuOpen: function (ev) {
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt = new qx.ui.menu.Button(this.tr("New filter"));
            bt.addListenerOnce("execute", this.__newFilter, this);
            menu.add(bt);
            if (rd != null) {
                bt = new qx.ui.menu.Button(this.tr("Edit"));
                bt.addListenerOnce("execute", this.__editFilter, this);
                menu.add(bt);

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
            var dlg = new ncms.mtt.filters.MttFilterDlg(this.tr("New filter"), {
                ruleId: this.getRuleId(),
                enabled: true
            });
            dlg.addListenerOnce("completed", function () {
                dlg.close();
                this.reload();
            }, this);
            dlg.open();
        },

        __removeFilter: function () {
            var rd = this.getSelectedRowData();
            qx.core.Assert.assertNotNull(rd);
            ncms.Application.confirm(this.tr("Are you sure to remove filter: %1", rd["type"]), function (yes) {
                if (!yes) {
                    return;
                }
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("mtt.filter.delete", {id: rd["id"]}), "DELETE");
                req.send(function () {
                    this.removeSelected();
                }, this);
            }, this);
        },

        __editFilter: function () {
            var rd = this.getSelectedRowData();
            qx.core.Assert.assertNotNull(rd);
            var dlg = new ncms.mtt.filters.MttFilterDlg(this.tr("Edit filter: %1", rd["type"]), rd);
            dlg.addListenerOnce("completed", function (ev) {
                dlg.close();
                this.reload();
            }, this);
            dlg.open();
        }
    },

    destruct: function () {
        this.__title = null;
        this.__delBt = null;
    }
});

