/**
 * @asset(ncms/icon/16/misc/image-plus.png)
 * @asset(ncms/icon/16/misc/images-stack.png)
 * @asset(ncms/icon/16/misc/film-plus.png)
 * @asset(ncms/icon/16/actions/delete.png)
 */
qx.Class.define("ncms.asm.am.MedialineAMValueWidget", {
    extend: sm.table.ToolbarLocalTable,
    implement: [qx.ui.form.IModel,
        ncms.asm.am.IValueWidget],
    include: [ncms.asm.am.MValueWidget,
        sm.table.MTableMutator],

    events: {
        /** Fired when the model data changes */
        "changeModel": "qx.event.type.Data"
    },


    construct: function (asmSpec, attrSpec) {
        this.__asmSpec = asmSpec;
        this.__attrSpec = attrSpec;
        this.__broadcaster = sm.event.Broadcaster.create({
            "del": false
        });

        this.base(arguments);
        this.set({height: 200});
        this._reload([]);
    },


    members: {

        __broadcaster: null,

        __asmSpec: null,

        __attrSpec: null,

        _createTable: function (tm) {
            var custom = tm.getCustom() || {};
            custom["selectionModel"] = function () {
                var res = new sm.table.selection.ExtendedSelectionModel();
                res.setSelectionMode(qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION);
                return res;
            };
            var table = new sm.table.Table(tm, custom);
            table.getSelectionModel().addListener("changeSelection", this.__syncState, this);
            table.set({
                showCellFocusIndicator: false,
                statusBarVisible: false,
                focusCellOnPointerMove: false
            });

            table.setContextMenu(new qx.ui.menu.Menu());
            table.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            return table;
        },

        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/misc/image-plus.png");
            bt.setToolTipText(this.tr("Add images to the gallery"));
            bt.addListener("execute", this.__addImages, this);
            part.add(bt);

            bt = this._createButton(null, "ncms/icon/16/misc/film-plus.png");
            bt.setToolTipText(this.tr("Add video into the gallery"));
            bt.addListener("execute", this.__addVideo, this);
            part.add(bt);

            bt = this._createButton(null, "ncms/icon/16/actions/delete.png");
            bt.setToolTipText(this.tr("Delete images and links"));
            bt.addListener("execute", this.__delete, this);
            this.__broadcaster.attach(bt, "del", "enabled");
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

        __beforeContextmenuOpen: function (ev) {
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("Add images"));
            bt.addListenerOnce("execute", this.__addImages, this);
            menu.add(bt);

            bt = new qx.ui.menu.Button(this.tr("Add video"));
            bt.addListenerOnce("execute", this.__addVideo, this);
            menu.add(bt);

            if (rd != null) {
                bt = new qx.ui.menu.Button(this.tr("Delete"));
                bt.addListenerOnce("execute", this.__delete, this);
                menu.add(bt);
            }
        },

        _setJsonTableData: function (tm, items) {
            var data = {
                "columns": [
                    {
                        "title": this.tr("Image/video").toString(),
                        "id": "resource",
                        "sortable": true,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Title").toString(),
                        "id": "description",
                        "sortable": false,
                        "width": "2*"
                    }
                ],
                "items": items
            };
            tm.setJsonData(data);
            this.__syncState();
        },

        __addImages: function () {
            var asmSpec = this.__asmSpec;
            var dlg = new ncms.mmgr.PageFilesSelectorDlg(
                asmSpec["id"],
                this.tr("Add images into the media gallery"), {
                    allowModify: true,
                    linkText: false,
                    smode: qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION
                });
            dlg.setCtypeAcceptor(ncms.Utils.isImageContentType.bind(ncms.Utils));
            dlg.addListener("completed", function (evt) {
                var files = dlg.getSelectedFiles();
                this.__pushFiles(files);
                dlg.close();
            }, this);
            dlg.open();
        },

        __pushFiles: function (files) {
            files.forEach(function (f) {
                this.addRow(f["id"], [f["name"], f["description"]]);
            }, this);
            this.fireEvent("modified");
        },

        __addVideo: function () {
            qx.log.Logger.info("Add video!!!");
        },


        __delete: function () {
            this.removeSelected();
            this.__syncState();
            this.fireEvent("modified");
        },

        __syncState: function () {
            var rd = this.getSelectedRowData();
            this.__broadcaster.setDel(rd != null);
        },

        setModel: function (model) {
            if (!Array.isArray(model)) {
                model = [];
            }
            this._reload(model);
            this.fireDataEvent("changeModel", model);
        },

        getModel: function () {
            var tm = this._table.getTableModel();
            if (tm == null) {
                return [];
            }
            return tm.getData();
        },

        resetModel: function () {
            this.setModel([]);
        }

    },

    destruct: function () {
        this.__asmSpec = null;
        this.__attrSpec = null;
        this._disposeObjects("__broadcaster");
    }
});