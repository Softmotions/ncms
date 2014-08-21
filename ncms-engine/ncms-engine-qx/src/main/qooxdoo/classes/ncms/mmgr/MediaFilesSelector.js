/**
 * Media files selector.
 * File list table with search field.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/folder-tree.png)
 * @asset(ncms/icon/16/misc/folder-tree-bw.png)
 * @asset(ncms/icon/16/misc/file-move.png)
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
        "fileSelected" : "qx.event.type.Data",

        /**
         * DATA: {
         *      id : {String} file attribute name
         *      value : {String} new value
         * }
         */
        "fileMetaEdited" : "qx.event.type.Data"
    },

    properties : {

        appearance : {
            refine : true,
            init : "ncms-mf-selector"
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

    /**
     * <code>
     *  {
     *      allowMove : true|false,
     *      allowSubfoldersView : true|fa;se
     *  }
     * </code>
     *
     * @param allowModify {Boolean?false} If true allow modify directory/files
     * @param constViewSpec {Object?}
     * @param opts {Object?} ncms.mmgr.MediaFilesSelector options.
     */
    construct : function(allowModify, constViewSpec, opts) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
        opts = this.__opts = opts || {};

        if (allowModify) {
            if (opts["allowMove"] === undefined) {
                opts["allowMove"] = true;
            }
        }
        if (opts["allowSubfoldersView"] === undefined) {
            opts["allowSubfoldersView"] = true;
        }

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function() {
            this.__search(null);
        }, this);
        sf.addListener("input", function(ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("keypress", function(ev) {
            if ("Down" === ev.getKeyIdentifier()) {
                this.__table.handleFocus();
            }
        }, this);

        this.addListener("appear", function() {
            sf.focus();
        });

        var smode = opts["smode"];
        if (smode == null) {
            smode = qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION;
        }

        this.__allowModify = !!allowModify;
        var ecolumns = this.__allowModify ? ["name", "description"] : null;
        var table = this.__table = new ncms.mmgr.MediaFilesTable(null, null, ecolumns, smode)
                .set({
                    "statusBarVisible" : true,
                    "showCellFocusIndicator" : false});

        table.getSelectionModel().addListener("changeSelection", function(ev) {
            this.__updateState();
            var file = this.__table.getSelectedFile();
            this.fireDataEvent("fileSelected", file ? file : null);
        }, this);


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
        if (this.__allowModify) {
            table.addListener("dataEdited", function(ev) {
                var tm = table.getTableModel();
                var data = ev.getData();
                var fspec = tm.getRowData(data.row);
                if (fspec == null) {
                    return;
                }
                var attrName = table.getTableModel().getColumnId(data.col);
                if (!this.__checkEditAccess([fspec])) {
                    tm.updateCachedRows(function(ind, rowdata) {
                        if (fspec["id"] === rowdata["id"]) {
                            rowdata[attrName] = data.oldValue;
                            return rowdata;
                        }
                    }, this);
                } else {
                    this.__updateMetaAttribute(fspec, attrName, data.value, data.oldValue);
                }
            }, this);
        }
    },

    members : {

        __allowModify : false,

        __dropFun : null,

        __sf : null,

        __table : null,

        __rmBt : null,

        __mvBt : null,

        __subfoldersBt : null,

        __opts : null,

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
            if (meta == null) {
                return;
            }
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

        getSelectedFiles : function() {
            return this.__table.getSelectedFiles();
        },

        _setupToolbar : function() {
            var toolbar = new qx.ui.toolbar.ToolBar().set({"appearance" : "toolbar-table/toolbar"});

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

                if (this.__opts["allowMove"]) {
                    this.__mvBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/file-move.png")
                            .set({"appearance" : "toolbar-table-button"});
                    bt.setToolTipText(this.tr("Move files"));
                    bt.addListener("execute", this.__moveFiles, this);
                    part.add(bt);
                }

                this._setupToolbarEditDelegate(part);
            }

            toolbar.add(new qx.ui.core.Spacer(), {flex : 1});

            part = new qx.ui.toolbar.Part()
                    .set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            if (this.__opts["allowSubfoldersView"]) {
                this.__subfoldersBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/folder-tree-bw.png")
                        .set({"appearance" : "toolbar-table-button"});
                bt.addListener("execute", this.__changeIncludeSubfolders, this);
                bt.setToolTipText(this.tr("Show subfolders files"));
                part.add(bt);
            }
            this._add(toolbar);
        },


        /**
         * Toolbar setup delegate for inheritors.
         * @param part {qx.ui.toolbar.Part}
         * @protected
         */
        _setupToolbarEditDelegate : function(part) {
        },


        __search : function(val) {
            this.updateViewSpec({stext : val || ""});
        },

        __applyItem : function(item) {
            if (item != null && (item["status"] & 1) != 0) { //folder
                var folder = "/" + item["path"].join("/");
                this.setConstViewSpec({"folder" : folder, "status" : 0, fts : true});
            } else {
                this.setConstViewSpec({"status" : 0, fts : true});
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
            var dlg = new ncms.mmgr.MediaItemRenameDlg(path, f.name);
            dlg.setPosition("bottom-center");
            dlg.addListener("completed", function() {
                dlg.close();
                this.reload();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
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
            if (this.__mvBt) {
                this.__mvBt.setEnabled(selected);
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
            dlg.addListener("completed", function() {
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

        __previewFile : function(ev) {
            var f = this.__table.getSelectedFile();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = (f.folder + f.name).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.file", path);
            qx.bom.Window.open(url + "?inline=true");
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

        __moveFiles : function(ev) {
            var sfiles = this.__table.getSelectedFiles();
            if (sfiles.length == 0) {
                return;
            }

            var dlg = new ncms.mmgr.MediaSelectFolderDlg(
                            sfiles.length == 1 ? this.tr("Move '%1' to another folder", sfiles[0].folder + sfiles[0].name) : this.tr("Move %1 files to another folder", sfiles.length)
            );

            var moveFiles = function(files, target, iter, cb, self) {
                if (files.length == 0) {
                    cb.call(self || this);
                    return;
                }

                var file = files.pop();
                var path = [].concat(file.folder.split('/').splice(0), file.name);
                var npath = [].concat(target, file.name);
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move", path), "PUT", "application/json");
                req.setData(npath.join("/"));
                req.send(function() {
                    iter.call(self || this, files, target, iter, cb, self);
                }, this);
            };

            dlg.addListener("completed", function(ev) {
                var target = ev.getData();
                moveFiles.call(this, sfiles, target, moveFiles, function() {
                    this.reload();
                    dlg.close();
                }, this);
            }, this);
            dlg.open();

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
                    bt = new qx.ui.menu.Button(this.tr("Preview"));
                    bt.addListener("execute", this.__previewFile, this);
                    menu.add(bt);

                    bt = new qx.ui.menu.Button(this.tr("Download"));
                    bt.addListener("execute", this.__downloadFile, this);
                    menu.add(bt);
                }

                if (this.__checkEditAccess(this.__table.getSelectedFiles())) {
                    menu.add(new qx.ui.menu.Separator());

                    if (selectedSingle) {
                        bt = new qx.ui.menu.Button(this.tr("Rename"));
                        bt.addListenerOnce("execute", this.__renameFile, this);
                        menu.add(bt);
                    }

                    if (this.__opts["allowMove"]) {
                        bt = new qx.ui.menu.Button(this.tr("Move to another folder"));
                        bt.addListenerOnce("execute", this.__moveFiles, this);
                        menu.add(bt);
                    }

                    bt = new qx.ui.menu.Button(this.tr("Remove"));
                    bt.addListenerOnce("execute", this.__rmFiles, this);
                    menu.add(bt);
                }
            }

            this._setupContextMenuDelegate(menu);

            return true;
        },


        /**
         * @param menu {qx.ui.menu.Menu} Context menu
         * @protected
         */
        _setupContextMenuDelegate : function(menu) {

        },

        __checkEditAccess : function(selected) {
            var appState = ncms.Application.APP_STATE;
            var user = appState.getUserLogin();
            for (var i = 0; i < selected.length; ++i) {
                var file = selected[i];
                // TODO: admin  role name to config/constant
                if (!appState.userHasRole("admin") && file["owner"] != user) {
                    return false;
                }
            }

            return true;
        },

        __updateMetaAttribute : function(spec, attr, value, oldValue) {
            var req;
            if ("name" == attr) {
                if (spec.folder == null || spec.name == null) {
                    return;
                }

                var npath = (spec.folder + spec.name).split("/");
                req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move", npath.slice(0, -1).concat(oldValue)), "PUT", "application/json");
                req.setData(npath.join("/"));
            } else {
                req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.meta", {"id" : spec["id"]}), "POST", "application/json");
                req.setParameter(attr, value, true);
            }

            req.send(function() {
                if (this.hasListener("fileMetaEdited")) {
                    this.fireDataEvent("fileMetaEdited", {id : attr, value : value});
                }
            }, this);
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
        this.__mvBt = null;
        this.__subfoldersBt = null;
        this.__opts = null;
    }
});