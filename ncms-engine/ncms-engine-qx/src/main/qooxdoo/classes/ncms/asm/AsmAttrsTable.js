/**
 * Assembly parents editor
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/actions/application_form_edit.png)
 * @asset(ncms/icon/16/misc/asterisk.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 */
qx.Class.define("ncms.asm.AsmAttrsTable", {
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

    events: {
        "attributesChanged": "qx.event.type.Event"
    },

    construct: function () {
        this.base(arguments);
        this.set({allowGrowX: true, allowGrowY: true});
        this._reload([]);
        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onAdd, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onRemove, this);
    },

    members: {

        __editBt: null,

        __delBt: null,

        __spec: null,

        setAsmSpec: function (spec) {
            this.__spec = spec;
            var items = [];
            if (this.__spec == null || !Array.isArray(spec["effectiveAttributes"])) {
                this._reload(items);
                return;
            }
            var attrs = spec["effectiveAttributes"];
            attrs.forEach(function (el) {
                var icon = el["overridden"] ? "ncms/icon/16/misc/asterisk.png" : "";
                var row = [icon, el["name"], el["label"], el["type"], el["value"]];
                items.push([row, el]);
            }, this);
            this._reload(items);
        },

        //overridden
        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);
            var el = this._createButton(null, "ncms/icon/16/actions/add.png",
                this.__onAdd, this);
            el.setToolTipText(this.tr("Add new attribute"));
            part.add(el);

            this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png",
                this.__onRemove, this);
            this.__delBt.setToolTipText(this.tr("Drop attribute"));
            part.add(this.__delBt);

            this.__editBt = this._createButton(null, "ncms/icon/16/actions/application_form_edit.png",
                this.__onEditOrOverride, this);
            part.add(this.__editBt);

            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});

            part = new qx.ui.toolbar.Part()
            .set({"appearance": "toolbar-table/part"});
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

        //overridden
        _setJsonTableData: function (tm, items) {
            var data = {
                "title": "",
                "columns": [
                    {
                        "title": "",
                        "id": "icon",
                        "sortable": false,
                        "width": 30,
                        "type": "image"
                    },
                    {
                        "title": this.tr("Name").toString(),
                        "id": "name",
                        "sortable": false,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Label").toString(),
                        "id": "label",
                        "sortable": false,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Type").toString(),
                        "id": "type",
                        "sortable": false,
                        "width": 90
                    },
                    {
                        "title": this.tr("Value").toString(),
                        "id": "value",
                        "sortable": false,
                        "width": "1*"
                    }
                ],
                "items": items
            };
            tm.setJsonData(data);
            this._syncState();
        },

        /**
         * @param tableModel {sm.model.JsonTableModel}
         */
        _createTable: function (tableModel) {
            var table = new sm.table.Table(tableModel, tableModel.getCustom());

            var colorm = qx.theme.manager.Color.getInstance();
            var rr = new sm.table.renderer.CustomRowRenderer();
            rr.setBgColorInterceptor(qx.lang.Function.bind(function (rowInfo) {
                var rdata = rowInfo.rowData.rowData;
                if (rdata["asmId"] !== this.__spec["id"]) {
                    return colorm.resolve("table-row-gray");
                } else {
                    return colorm.resolve("background");
                }
            }, this));
            table.setDataRowRenderer(rr);
            table.addListener("cellDbltap", this.__onEditOrOverride, this);
            table.set({
                showCellFocusIndicator: false,
                statusBarVisible: true,
                focusCellOnPointerMove: false
            });

            table.getSelectionModel().addListener("changeSelection", this._syncState, this);

            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            this._registerCommandFocusWidget(table);
            return table;
        },

        _syncState: function () {
            var sInd = this.getSelectedRowIndex();
            var rd = this.getSelectedRowData();
            this.__delBt.setEnabled(rd != null && rd["asmId"] == this.__spec["id"]);
            this.__editBt.setEnabled(rd != null);
            if (rd) {
                if (rd["asmId"] === this.__spec["id"]) {
                    this.__editBt.setToolTipText(this.tr("Edit attribute"));
                } else {
                    this.__editBt.setToolTipText(this.tr("Override attribute"));
                }
            }
            this.__upBt.setEnabled(!!this.__canMove(sInd, 1));
            this.__downBt.setEnabled(!!this.__canMove(sInd, -1));
        },

        __beforeContextmenuOpen: function (ev) {
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt = new qx.ui.menu.Button(this.tr("New"));
            bt.addListenerOnce("execute", this.__onAdd, this);
            menu.add(bt);

            if (rd != null) {

                if (rd["asmId"] === this.__spec["id"]) {

                    bt = new qx.ui.menu.Button(this.tr("Edit"));
                    bt.addListenerOnce("execute", this.__onEdit, this);
                    menu.add(bt);

                    bt = new qx.ui.menu.Button(this.tr("Remove"));
                    bt.addListenerOnce("execute", this.__onRemove, this);
                    menu.add(bt);

                } else {

                    bt = new qx.ui.menu.Button(this.tr("Override"));
                    bt.addListenerOnce("execute", this.__onEditOrOverride, this);
                    menu.add(bt);

                    bt = new qx.ui.menu.Button(this.tr("Edit in parent"));
                    bt.addListenerOnce("execute", this.__onEdit, this);
                    menu.add(bt);
                }
            }
        },

        __onMoveUp: function () {
            this.__move(this.getSelectedRowIndex(), 1);
        },

        __onMoveDown: function () {
            this.__move(this.getSelectedRowIndex(), -1);
        },

        __move: function (sInd, dir) {
            var rd = this.getRowData(sInd);
            var buddy = this.__canMove(sInd, dir);
            if (!buddy) {
                return;
            }
            var buddyInd = (dir == 1) ? sInd - 1 : sInd + 1;
            var url = ncms.Application.ACT.getRestUrl("asms.attributes.exchange",
                {
                    "ordinal1": rd["ordinal"],
                    "ordinal2": buddy["ordinal"]
                });
            var req = new sm.io.Request(url, "PUT");
            req.send(function () {
                var data = this.getTableModel().getData();
                qx.core.Assert.assertTrue(data[sInd] != null && data[buddyInd] != null);
                var tmp = data[sInd];
                data[sInd] = data[buddyInd];
                data[buddyInd] = tmp;
                tmp = rd["ordinal"];
                rd["ordinal"] = buddy["ordinal"];
                buddy["ordinal"] = tmp;
                this.getTableModel().setData(data);
                this.getSelectionModel().setSelectionInterval(buddyInd, buddyInd);
            }, this);
        },


        __canMove: function (sInd, dir) {
            if (sInd == null || sInd === -1) {
                return false;
            }
            var rd = this.getRowData(sInd);
            if (rd == null) {
                return false;
            }
            var isValidBuddy = function (buddy) {
                return !(buddy["asmId"] !== this.__spec["id"] || buddy["overridden"]);
            }.bind(this);

            if (!isValidBuddy(rd)) {
                return false;
            }
            var data = this.getTableModel().getData();
            var buddy;
            if (dir === 1) {
                if (sInd === 0) {
                    return false;
                }
                buddy = this.getRowData(sInd - 1);
            } else {
                if (sInd === data.length - 1) {
                    return false;
                }
                buddy = this.getRowData(sInd + 1);
            }
            if (isValidBuddy(buddy)) {
                return buddy;
            }
            return false;
        },

        __onAdd: function () {
            var dlg = new ncms.asm.AsmAttrEditorDlg(
                this.tr("New attribute for assembly: %1", this.__spec["name"]),
                this.__spec
            );
            dlg.addListenerOnce("completed", function (ev) {
                dlg.close();
                this.fireEvent("attributesChanged");
            }, this);
            dlg.open();
        },

        __onRemove: function () {
            var rd = this.getSelectedRowData();
            if (rd == null || rd["asmId"] != this.__spec["id"]) {
                return;
            }
            ncms.Application.confirmCb(
                this.tr("A you sure to remove attribute?"),
                this.tr("Recursive?"),
                false,
                function (yes, recursive) {
                    if (!yes) {
                        return;
                    }
                    var req = ncms.Application.request("asms.attribute",
                        {id: rd["asmId"], name: rd["name"]},
                        "DELETE"
                    );
                    req.setParameter("recursive", !!recursive);
                    req.send(function () {
                        this.fireEvent("attributesChanged");
                    }, this);
                },
                this
            );
        },

        __onEdit: function (ev, rd) {
            rd = (rd == null) ? this.getSelectedRowData() : rd;
            if (rd == null) {
                return;
            }
            var caption;
            if (rd.override === true) {
                caption = this.tr("Override the parent assembly attribute: '%1'", rd["name"]);
            } else if (rd["asmId"] != this.__spec["id"]) {
                caption = this.tr("Edit the parent assembly attribute: '%1'", rd["name"]);
            } else {
                caption = this.tr("Edit attribute: '%1'", rd["name"]);
            }

            var dlg = new ncms.asm.AsmAttrEditorDlg(caption, this.__spec, rd);
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this.fireEvent("attributesChanged");
            }, this);
            dlg.open();
        },


        __onEditOrOverride: function (ev) {
            var rd = this.getSelectedRowData();
            if (rd == null) {
                return;
            }
            if (rd["asmId"] != this.__spec["id"]) {
                rd = sm.lang.Object.shallowClone(rd);
                rd["asmId"] = this.__spec["id"];
                rd.override = true;
            }
            this.__onEdit(ev, rd);
        }

    },

    destruct: function () {
        this.__spec = null;
        this.__delBt = null;
        this.__editBt = null;
        this.__upBt = null;
        this.__downBt = null;
        //this._disposeObjects("__field_name");
    }
});