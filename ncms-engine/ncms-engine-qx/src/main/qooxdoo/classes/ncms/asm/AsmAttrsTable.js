/**
 * Assembly parents editor
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/actions/application_form_edit.png)
 * @asset(ncms/icon/16/misc/flow_block.png)
 */
qx.Class.define("ncms.asm.AsmAttrsTable", {
    extend : sm.table.ToolbarLocalTable,
    implement : [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include : [
        sm.ui.form.MStringForm,
        sm.table.MTableMutator
    ],

    events : {
        "attributesChanged" : "qx.event.type.Event"
    },

    construct : function() {
        this.base(arguments);
        this.set({allowGrowX : true, allowGrowY : false, height : 200});
        this._reload([]);
    },

    members : {

        __editBt : null,

        __delBt : null,

        __spec : null,

        setAsmSpec : function(spec) {
            this.__spec = spec;
            var items = [];
            if (this.__spec == null || !Array.isArray(spec["effectiveAttributes"])) {
                this._reload(items);
                return;
            }
            var attrs = spec["effectiveAttributes"];
            attrs.forEach(function(el) {
                var icon = el["overriden"] ? "ncms/icon/16/misc/flow_block.png" : "";
                var row = [icon, el["name"], el["value"], el["type"]];
                items.push([row, el]);
            }, this);
            this._reload(items);
        },

        //overriden
        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);
            var el = this._createButton(null, "ncms/icon/16/actions/add.png",
                    this.__onAdd, this);
            el.setToolTipText(this.tr("Add new attribute"));
            part.add(el);

            this.__editBt = this._createButton(null, "ncms/icon/16/actions/application_form_edit.png",
                    this.__onEditOrOverride, this);
            part.add(this.__editBt);

            this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png",
                    this.__onRemove, this);
            this.__delBt.setToolTipText(this.tr("Drop attribute"));
            part.add(this.__delBt);


            return toolbar;
        },

        _createButton : function(label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance" : "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        //overriden
        _setJsonTableData : function(tm, items) {
            var data = {
                "title" : "",
                "columns" : [
                    {
                        "title" : "",
                        "id" : "icon",
                        "sortable" : false,
                        "width" : 40,
                        "type" : "image"
                    },
                    {
                        "title" : this.tr("Name").toString(),
                        "id" : "name",
                        "sortable" : false,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Value").toString(),
                        "id" : "value",
                        "sortable" : false,
                        "width" : "3*"
                    },
                    {
                        "title" : this.tr("Type").toString(),
                        "id" : "type",
                        "sortable" : false,
                        "width" : "1*"
                    }
                ],
                "items" : items
            };
            tm.setJsonData(data);
            this._syncState();
        },

        //overriden
        _createTable : function(tableModel) {
            var table = new sm.table.Table(tableModel, tableModel.getCustom());

            var colorm = qx.theme.manager.Color.getInstance();
            var rr = new sm.table.renderer.CustomRowRenderer();
            rr.setBgColorInterceptor(qx.lang.Function.bind(function(rowInfo) {
                var rdata = rowInfo.rowData.rowData;
                if (rdata["asmId"] !== this.__spec["id"]) {
                    return colorm.resolve("table-row-gray");
                } else {
                    return colorm.resolve("background");
                }
            }, this));
            table.setDataRowRenderer(rr);
            table.addListener("cellDblclick", this.__onEditOrOverride, this);
            table.set({
                showCellFocusIndicator : false,
                statusBarVisible : true,
                focusCellOnMouseMove : false});

            table.getSelectionModel()
                    .addListener("changeSelection", this._syncState, this);


            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

            return table;
        },

        _syncState : function() {
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
        },

        __beforeContextmenuOpen : function(ev) {
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

        __onAdd : function() {
            var dlg = new ncms.asm.AsmAttrEditorDlg(
                    this.tr("New attribute for assembly: %1", this.__spec["name"]),
                    this.__spec
            );
            dlg.addListenerOnce("completed", function(ev) {
                dlg.close();
                this.fireEvent("attributesChanged");
            }, this);
            dlg.open();
        },

        __onRemove : function() {
            var rd = this.getSelectedRowData();
            if (rd == null || rd["asmId"] != this.__spec["id"]) {
                return;
            }
            var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("asms.attribute",
                            {id : rd["asmId"], name : rd["name"]}),
                    "DELETE", "application/json");
            req.send(function() {
                this.fireEvent("attributesChanged");
            }, this);
        },

        __onEdit : function(ev, rd) {
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
            dlg.addListenerOnce("completed", function(ev) {
                dlg.close();
                this.fireEvent("attributesChanged");
            }, this);
            dlg.open();
        },


        __onEditOrOverride : function(ev) {
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

    destruct : function() {
        this.__spec = null;
        this.__delBt = null;
        this.__editBt = null;
        //this._disposeObjects("__field_name");
    }
});