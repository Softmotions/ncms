/**
 * Media files selector.
 * File list table with search field.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/folder-tree.png)
 * @asset(ncms/icon/16/misc/folder-tree-bw.png)
 */
qx.Class.define("ncms.mmgr.MediaFilesSelector", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {

        /**
         * DATA: {
         *   id : {Integer} File ID
         *   name : {String} File name
         *   content_type : {String} File content type
         *   content_length : {Integer} File data length
         *   folder : {String} Full path to file folder
         *   status : {Integer} 1 - folder, 0 - file
         * }
         * or null
         */
        "fileSelected" : "qx.event.type.Data"
    },

    properties : {

        appearance : {
            refine : true,
            init : "mf-selector"
        },

        /**
         * Set media item:
         *
         *  item = {
         *        "label"  : {String} Item name.
         *        "status" : {Number} Item status. (1 - folder, 0 - file)
         *        "path"   : {Array} Path to the item (from tree root)
         *  };
         */
        "item" : {
            check : "Object",
            nullable : true,
            apply : "__applyItem"
        }
    },

    construct : function(allowModify, constViewSpec, smode) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function() {
            this.__search(null);
        }, this);
        sf.addListener("input", function(ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("changeValue", function(ev) {
            this.__search(ev.getData());
        }, this);

        if (smode == null) {
            smode = qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION;
        }
        var table = this.__table = new ncms.mmgr.MediaFilesTable(null, null, smode)
                .set({
                    "statusBarVisible" : true,
                    "showCellFocusIndicator" : false});

        table.getSelectionModel().addListener("changeSelection", function(ev) {
            this.__updateState();
            var file = this.__table.getSelectedFile();
            this.fireDataEvent("fileSelected", file ? file : null);
        }, this);


        this.__allowModify = !!allowModify;
        this.__dropFun = this.__handleDropFiles.bind(this);

        this._add(this.__sf);
        this._setupToolbar();
        this._add(table, {flex : 1});

        if (constViewSpec != null) {
            this.setConstViewSpec(constViewSpec);
        }

        this.addListener("appear", function() {
            this.__ensureUploadControls();
            this.__updateState();
        }, this);

        table.getTableModel().addListener("viewSpecChanged", this.__viewSpecChanged, this);

        if (this.__allowModify) {
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
        }
    },

    members : {

        __allowModify : false,

        __dropFun : null,

        __sf : null,

        __table : null,

        __rmBt : null,

        __subfoldersBt : null,

        setViewSpec : function(vs) {
            this.__table.resetSelection();
            this.__table.getTableModel().setViewSpec(vs);
        },

        updateViewSpec : function(vs) {
            this.__table.resetSelection();
            this.__table.getTableModel().updateViewSpec(vs);
        },

        setConstViewSpec : function(vs) {
            this.__table.resetSelection();
            this.__table.getTableModel().setConstViewSpec(vs);
        },

        reload : function() {
            this.__table.resetSelection();
            this.__table.getTableModel().reloadData();
        },

        resetSelection : function() {
            this.__table.resetSelection();
        },

        getTable : function() {
            return this.__table;
        },

        setUpdateFileMeta : function(meta) {
            var tm = this.__table.getTableModel();
            tm.updateCachedRows(function(ind, rowdata) {
                if (meta["id"] === rowdata["id"]) {
                    return meta;
                }
            }, this);
        },

        getSelectedFile : function() {
            return this.__table.getSelectedFile()
        },

        _setupToolbar : function() {
            var toolbar = new qx.ui.toolbar.ToolBar();

            if (this.__allowModify) {
                var part = new qx.ui.toolbar.Part()
                        .set({"appearance" : "toolbar-table/part"});
                toolbar.add(part);

                var bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/add.png")
                        .set({"appearance" : "toolbar-table-button"});
                bt.setToolTipText(this.tr("Add files"));
                bt.addListener("execute", this.__addFiles, this);
                part.add(bt);

                this.__rmBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/delete.png")
                        .set({"appearance" : "toolbar-table-button"});
                bt.setToolTipText(this.tr("Remove files"));
                bt.addListener("execute", this.__rmFiles, this);
                part.add(bt);
            }

            toolbar.add(new qx.ui.core.Spacer(), {flex : 1});

            part = new qx.ui.toolbar.Part()
                    .set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);
            this.__subfoldersBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/folder-tree-bw.png")
                    .set({"appearance" : "toolbar-table-button"});
            bt.addListener("execute", this.__changeIncludeSubfolders, this);
            bt.setToolTipText(this.tr("Show subfolders files"));
            part.add(bt);

            this._add(toolbar);
        },


        __search : function(val) {
            this.updateViewSpec({stext : val || ""});
        },

        __applyItem : function(item) {
            if (item != null && item["status"] == 1) { //folder
                var folder = "/" + item["path"].join("/");
                this.setConstViewSpec({"folder" : folder, "status" : 0, fts : true});
            } else {
                this.setConstViewSpec({"folder" : "/", "status" : 0, fts : true});
            }
        },

        __addFiles : function() {
            var form = document.getElementById("ncms-upload-form");
            form.reset();
            var input = document.getElementById("ncms-upload-file");
            input.onchange = this.__handleFormUploadFiles.bind(this);
            input.click();
        },

        __renameFile : function(ev) {
            var f = this.__table.getSelectedFile();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = f.folder + f.name;
            path = path.split("/");
            var d = new ncms.mmgr.MediaItemRenameDlg(path, f.name);
            d.setPosition("bottom-center");
            d.addListenerOnce("completed", function() {
                d.hide();
                this.reload();
            }, this);
            d.placeToWidget(ev.getTarget(), false);
            d.show();
        },

        __rmFiles : function() {
            var sfiles = this.__table.getSelectedFiles();
            var paths = [];
            sfiles.forEach(function(f) {
                if (f.folder != null && f.name != null) {
                    paths.push(f.folder + f.name);
                }
            });
            if (paths.length == 0) {
                return;
            }
            ncms.Application.confirm(this.tr("Are you sure to remove selected files?"), function(yes) {
                if (!yes) return;
                var url = ncms.Application.ACT.getUrl("media.delete-batch");
                var req = new sm.io.Request(url, "DELETE", "application/json");
                req.setData(JSON.stringify(paths));
                req.setRequestHeader("Content-Type", "application/json");
                req.send(function(resp) {
                    this.reload();
                }, this);
            }, this);
        },

        __viewSpecChanged : function(ev) {
            var vs = ev.getData() || {};
            var bt = this.__subfoldersBt;
            if (bt) {
                if (vs["subfolders"] == true) {
                    bt.addState("checked");
                    bt.setIcon("ncms/icon/16/misc/folder-tree.png");
                } else {
                    bt.removeState("checked");
                    bt.setIcon("ncms/icon/16/misc/folder-tree-bw.png");
                }
            }
        },

        __changeIncludeSubfolders : function(ev) {
            var bt = ev.getTarget();
            this.updateViewSpec({"subfolders" : !bt.hasState("checked")});
        },

        __updateState : function() {
            var selected = !this.__table.getSelectionModel().isSelectionEmpty();
            if (this.__rmBt) {
                this.__rmBt.setEnabled(selected);
            }
        },

        __handleFormUploadFiles : function(ev) {
            var input = ev.target;
            input.onchange = null;
            this.__uploadFiles(input.files);
        },

        __handleDropFiles : function(ev) {
            ev.stopPropagation();
            ev.preventDefault();
            this.__uploadFiles(ev.dataTransfer.files);
        },

        __uploadFiles : function(files, cb, self) {
            if (files == null) {
                return;
            }
            var fcnt = 0;
            while (files[fcnt]) fcnt++;
            if (fcnt === 0) { //nothing to upload
                return;
            }
            var path = (this.getItem() != null) ? this.getItem()["path"] : [];
            var dlg = new sm.ui.upload.FileUploadProgressDlg(function(f) {
                return ncms.Application.ACT.getRestUrl("media.upload", path.concat(f.name));
            }, files);
            dlg.addListenerOnce("completed", function() {
                dlg.close();
                this.reload();
                if (cb != null) {
                    cb.call(self);
                }
            }, this);
            dlg.open();
        },

        __ensureUploadControls : function() {
            if (!this.__allowModify) { //we are in read-only mode
                return;
            }
            var el = this.getContentElement().getDomElement();
            if (el.ondrop == this.__dropFun) {
                return;
            }
            el.ondrop = this.__dropFun;
            el.ondragover = function() {
                return false;
            };
        },

        __downloadFile : function(ev) {
            var f = this.__table.getSelectedFile();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = (f.folder + f.name).split("/");
            var form = document.getElementById("ncms-download-form");
            form.action = ncms.Application.ACT.getRestUrl("media.file", path);
            form.submit();
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var selected = !this.__table.getSelectionModel().isSelectionEmpty();
            var selectedSingle = (selected && this.__table.getSelectionModel().getSelectedCount() == 1);

            var bt = new qx.ui.menu.Button(this.tr("Upload"));
            bt.addListenerOnce("execute", this.__addFiles, this);
            menu.add(bt);

            if (selected) {
                if (selectedSingle) {
                    bt = new qx.ui.menu.Button(this.tr("Download"));
                    bt.addListener("execute", this.__downloadFile, this);
                    menu.add(bt);
                }

                menu.add(new qx.ui.menu.Separator());

                if (selectedSingle) {
                    bt = new qx.ui.menu.Button(this.tr("Rename"));
                    bt.addListenerOnce("execute", this.__renameFile, this);
                    menu.add(bt);
                }
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__rmFiles, this);
                menu.add(bt);
            }
            return true;
        }
    },

    destruct : function() {
        if (this.getContentElement() != null) {
            var el = this.getContentElement().getDomElement();
            el.ondrop = null;
            el.ondragover = null;
            el.ondragenter = null;
            el.ondragleave = null;
        }
        this.__sf = null;
        this.__table = null;
        this.__dropFun = null;
        this.__rmBt = null;
        this.__subfoldersBt = null;
    }
});