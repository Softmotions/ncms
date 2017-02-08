/**
 * Media files selector.
 * File list table with search field.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/actions/edit-document.png)
 * @asset(ncms/icon/16/misc/folder-tree.png)
 * @asset(ncms/icon/16/misc/folder-tree-bw.png)
 * @asset(ncms/icon/16/misc/file-move.png)
 * @asset(ncms/icon/16/misc/home.png)
 * @asset(ncms/icon/16/misc/home-bw.png)
 */
qx.Class.define("ncms.mmgr.MediaFilesSelector", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.MCommands],


    statics: {},

    events: {

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
        "fileSelected": "qx.event.type.Data",

        /**
         * DATA: {
         *      id : {String} file attribute name
         *      value : {String} new value
         * }
         */
        "fileMetaEdited": "qx.event.type.Data"
    },

    properties: {

        appearance: {
            refine: true,
            init: "ncms-mf-selector"
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
        "item": {
            check: "Object",
            nullable: true,
            apply: "__applyItem"
        },

        /**
         * If `true` in-page mode is activated.
         */
        "inpage": {
            check: "Boolean",
            nullable: false,
            init: false,
            event: "changeInpage",
            apply: "__applyInpage"
        }
    },

    /**
     * Options:
     * <code>
     *  {
     *      allowMove : true|false,
     *      allowSubfoldersView : true|false
     *      pageSpec: {id: page id, name: page name} Optional page spec to show files in page
     *  }
     * </code>
     *
     * @param allowModify {Boolean?false} If true allow modify directory/files
     * @param constViewSpec {Object?}
     * @param opts {Object?} ncms.mmgr.MediaFilesSelector options.
     */
    construct: function (allowModify, constViewSpec, opts) {
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

        this.__dropZone = new ncms.cc.WidgetHighlighter(this);
        var me = this;
        window.addEventListener("dragover", function (ev) {
            ev.preventDefault();
            me.__dropZone.show();
        }, false);
        window.addEventListener("dragleave", function (ev) {
            ev.preventDefault();
            me.__dropZone.hide();
        }, false);
        window.addEventListener("drop", function (ev) {
            ev.preventDefault();
            me.__dropZone.hide();
        }, false);

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function () {
            this.__search(null);
        }, this);
        sf.addListener("input", function (ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("keypress", function (ev) {
            if ("Down" === ev.getKeyIdentifier()) {
                this.__table.handleFocus();
            }
        }, this);

        this.addListener("appear", function () {
            sf.focus();
        });

        var smode = opts["smode"];
        if (smode == null) {
            smode = qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION;
        }

        this.__dropFun = this.__handleDropFiles.bind(this);
        this.__allowModify = !!allowModify;

        var ecolumns = this.__allowModify ? ["name", "description"] : null;
        var table = this.__table = new ncms.mmgr.MediaFilesTable(null, null, ecolumns, smode)
        .set({
            "statusBarVisible": true,
            "showCellFocusIndicator": false
        });

        var events = ncms.Events.getInstance();
        events.addListener("mediaUpdated", this.__handleMediaUpdated, this);
        events.addListener("mediaRemoved", this.__handleMediaRemoved, this);
        table.getSelectionModel().addListener("changeActualSelection", this.__onChangeSelection, this);

        if (constViewSpec != null) {
            this.setConstViewSpec(constViewSpec);
        }

        this._add(this.__sf);
        this._setupToolbar();
        this._add(table, {flex: 1});


        this.addListener("appear", function () {
            this.__ensureUploadControls();
            this.__updateState();
        }, this);

        table.getTableModel().addListener("viewSpecChanged", this.__viewSpecChanged, this);

        if (this.__allowModify) {
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
        }
        if (this.__allowModify) {
            table.addListener("dataEdited", function (ev) {
                var tm = table.getTableModel();
                var data = ev.getData();
                var fspec = tm.getRowData(data.row);
                if (fspec == null) {
                    return;
                }
                var attrName = table.getTableModel().getColumnId(data.col);
                if (!this.__checkEditAccess([fspec])) {
                    tm.updateCachedRows(function (ind, rowdata) {
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

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__rmFiles, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("F6"),
            this.__moveFiles, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNewFile, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("F4"),
            this.__onEdit, this);
        this._registerCommandFocusWidget(table);
    },

    members: {

        __allowModify: false,

        __dropFun: null,

        __dropZone: null,

        __sf: null,

        __table: null,

        __rmBt: null,

        __mvBt: null,

        __importBt: null,

        __editBt: null,

        __subfoldersBt: null,

        __inpageBt: null,

        __opts: null,

        setViewSpec: function (vs) {
            this.__table.getTableModel().setViewSpec(vs);
        },

        updateViewSpec: function (vs) {
            this.__table.getTableModel().updateViewSpec(vs);
        },

        setConstViewSpec: function (vs) {
            this.__table.getTableModel().setConstViewSpec(vs);
        },

        getConstViewSpec: function (vs) {
            return this.__table.getTableModel().getConstViewSpec(vs);
        },

        reload: function () {
            var table = this.__table;
            var selectFile = table.getSelectedFile();
            var tm = table.getTableModel();
            if (selectFile == null) {
                return tm.reloadData(false);
            }
            tm.addListenerOnce("rowsDataLoaded", function () {
                var selectedIdx = table.getSelectedFileInd();
                tm.iterateCachedRows(function (offset, item) {
                    if (selectFile.id == item.id && offset != selectedIdx) {
                        table.getSelectionModel().skipNextSelectionEventCnt = 1; // skip reselect to the offset
                        table.selectSingleRow(offset);            
                    }
                });
            }, this);
            tm.reloadData(false);
        },

        resetSelection: function () {
            this.__table.resetSelection();
        },

        getTable: function () {
            return this.__table;
        },

        setUpdateFileMeta: function (meta) {
            if (meta == null) {
                return;
            }
            var tm = this.__table.getTableModel();
            tm.updateCachedRows(function (ind, rowdata) {
                if (meta["id"] === rowdata["id"]) {
                    return meta;
                }
            }, this);
        },

        getSelectedFile: function () {
            return this.__table.getSelectedFile();
        },

        getSelectedFileInd: function () {
            return this.__table.getSelectedFileInd();
        },

        getSelectedFiles: function () {
            return this.__table.getSelectedFiles();
        },

        _setupToolbar: function () {
            var toolbar = new qx.ui.toolbar.ToolBar().set({"appearance": "toolbar-table/toolbar"});

            if (this.__allowModify) {
                var part = new qx.ui.toolbar.Part()
                .set({"appearance": "toolbar-table/part"});
                toolbar.add(part);

                var el = new qx.ui.toolbar.MenuButton(null, "ncms/icon/16/actions/add.png");
                var menu = new qx.ui.menu.Menu();

                var bt = new qx.ui.menu.Button(this.tr("Upload files"));
                bt.addListener("execute", this.__addFiles, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("New file"));
                bt.addListener("execute", this.__onNewFile, this);
                menu.add(bt);

                el.setToolTipText(this.tr("Add files"));
                el.setAppearance("toolbar-table-menubutton");
                el.setShowArrow(true);
                el.setMenu(menu);
                part.add(el);

                this.__rmBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/delete.png")
                .set({"appearance": "toolbar-table-button"});
                bt.setToolTipText(this.tr("Remove files"));
                bt.addListener("execute", this.__rmFiles, this);
                part.add(bt);

                this.__editBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/actions/edit-document.png")
                .set({"appearance": "toolbar-table-button"});
                bt.setToolTipText(this.tr("Edit file content"));
                bt.addListener("execute", this.__onEdit, this);
                part.add(bt);

                this.__importBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/document-import.png")
                .set({"appearance": "toolbar-table-button"});
                bt.setToolTipText(this.tr("Import from media repository"));
                bt.addListener("execute", this.__importFile, this);
                this.__importBt.exclude();
                this.__importBt.set({enabled: false});
                part.add(bt);

                if (this.__opts["allowMove"]) {
                    this.__mvBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/file-move.png")
                    .set({"appearance": "toolbar-table-button"});
                    bt.setToolTipText(this.tr("Move files"));
                    bt.addListener("execute", this.__moveFiles, this);
                    part.add(bt);
                }

                this._setupToolbarEditDelegate(part);
            }

            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});

            part = new qx.ui.toolbar.Part()
            .set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            if (this.__opts["pageSpec"]) {
                var pname = this.__opts["pageSpec"]["name"] || "";
                this.__inpageBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/home-bw.png")
                .set({"appearance": "toolbar-table-button"});
                bt.addListener("execute", function (ev) {
                    var bt = ev.getTarget();
                    this.setInpage(!bt.hasState("checked"));
                }, this);
                bt.setToolTipText(this.tr("Show files in page: %1", pname));
                part.add(bt);
            }

            if (this.__opts["allowSubfoldersView"]) {
                this.__subfoldersBt = bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/folder-tree-bw.png")
                .set({"appearance": "toolbar-table-button"});
                bt.addListener("execute", this.__changeIncludeSubfolders, this);
                bt.setToolTipText(this.tr("Show files in subfolders"));
                part.add(bt);
            }
            this._add(toolbar);
        },

        __applyInpage: function (val) {
            var bt = this.__inpageBt;
            if (!bt || !this.__opts["pageSpec"]) {
                return;
            }
            if (val) {
                this.updateViewSpec({"inpage": this.__opts["pageSpec"]["id"]});
            } else {
                this.updateViewSpec({"inpage": 0});
            }
        },

        /**
         * Toolbar setup delegate for inheritors.
         * @param part {qx.ui.toolbar.Part}
         * @protected
         */
        _setupToolbarEditDelegate: function (part) {
        },


        __search: function (val) {
            this.updateViewSpec({stext: val || ""});
        },

        _resolveViewSpec: function (item) {
            if (item != null && (item["status"] & 1) != 0) { //folder
                var folder = "/" + item["path"].join("/");
                return {"folder": folder, "status": 0, fts: true};
            } else {
                return {"status": 0, fts: true};
            }
        },

        __applyItem: function (item) {
            this.setConstViewSpec(this._resolveViewSpec(item));
        },

        __addFiles: function () {
            var form = document.getElementById("ncms-upload-form");
            form.reset();
            var input = document.getElementById("ncms-upload-file");
            input.onchange = this.__handleFormUploadFiles.bind(this);
            input.click();
        },

        __onNewFile: function (ev) {
            var path = this.__getParentPath();
            var dlg = new ncms.mmgr.MediaFileNewDlg(path);
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function () {
                dlg.close();
                this.reload();
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(this.__sf, false);
            }
            dlg.open();
        },

        __renameFile: function (ev) {
            var f = this.__table.getSelectedFile();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = f.folder + f.name;
            path = path.split("/");
            var dlg = new ncms.mmgr.MediaItemRenameDlg(path, f.name);
            dlg.setPosition("bottom-center");
            dlg.addListener("completed", function () {
                dlg.close();
                this.reload();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __rmFiles: function () {
            var sfiles = this.__table.getSelectedFiles();
            var paths = [];
            sfiles.forEach(function (f) {
                if (f.folder != null && f.name != null) {
                    paths.push(f.folder + f.name);
                }
            });
            if (paths.length == 0) {
                return;
            }

            ncms.Application.confirm(this.tr("Are you sure to remove selected files?"), function (yes) {
                if (!yes) return;
                var url = ncms.Application.ACT.getUrl("media.delete-batch");
                var req = new sm.io.Request(url, "DELETE", "application/json");
                req.setData(JSON.stringify(paths));
                req.setRequestHeader("Content-Type", "application/json");
                req.send(function (resp) {
                    this.reload();
                }, this);
            }, this);
        },

        __viewSpecChanged: function (ev) {
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
            bt = this.__inpageBt;
            if (bt) {
                if (vs["inpage"]) {
                    bt.addState("checked");
                    this.setInpage(true);
                    bt.setIcon("ncms/icon/16/misc/home.png");
                    this.__subfoldersBt.setEnabled(false);
                } else {
                    bt.removeState("checked");
                    this.setInpage(false);
                    bt.setIcon("ncms/icon/16/misc/home-bw.png");
                    this.__subfoldersBt.setEnabled(true);
                }
            }

            bt = this.__importBt;
            if (vs["inpages"]) {
                bt.show();
                bt.setEnabled(true);
            }
            if (vs["inpage"] != null) {
                bt.show();
                bt.setEnabled(vs["inpage"] > 0);
            }
        },

        __changeIncludeSubfolders: function (ev) {
            var bt = ev.getTarget();
            this.updateViewSpec({"subfolders": !bt.hasState("checked")});
        },

        __updateState: function () {
            var file = this.__table.getSelectedFile(),
                canEdit = !!(file && this.__checkEditAccess([file])),
                isTextual = !!(file && ncms.Utils.isTextualContentType(file["content_type"])),
                selectedSingle = (file && this.__table.getSelectionModel().getSelectedCount() == 1);

            if (this.__rmBt) {
                this.__rmBt.setEnabled(canEdit);
            }
            if (this.__mvBt) {
                this.__mvBt.setEnabled(canEdit);
            }
            if (this.__editBt) {
                this.__editBt.setEnabled(canEdit && selectedSingle && isTextual);
            }
        },


        __onChangeSelection: function (ev) {
            this.__updateState();
            var file = this.__table.getSelectedFile();
            this.fireDataEvent("fileSelected", file ? file : null);
        },


        __handleMediaUpdated: function (ev) {
            // {"uuid":"53cc5198-46c2-40ce-ba89-fb91d74a7527",
            //  "type":"MediaUpdateEvent",
            //  "user":"adam",
            //  "hints":{"app":"54f68f36-935c-4437-9729-5a124fb084c7"},
            //   "id":1123,"isFolder":false,
            //   "path":"/site/test/bar.txt"}
            // CVS={"folder":"/site/test","status":0,"fts":true}
            var cvs = this.getConstViewSpec();
            var data = ev.getData();
            if (data.isFolder
                || (cvs == null || sm.lang.String.isEmpty(cvs.folder))
                || data.path.indexOf(cvs.folder + '/') !== 0) {
                return;
            }
            var selectedFile = this.__table.getSelectedFile();
            var found = false;
            this.__table.getTableModel().iterateCachedRows(function (offset, item) {
                if (data.id == item.id) {
                    found = true;
                    var updateSelectedFile = (selectedFile != null
                    && selectedFile.id == item.id
                    && this.hasListener("fileMetaEdited"));
                    ncms.mmgr.MediaFilesUtils.fetchMediaInfo(item.id, function (meta) {
                        Object.keys(item).forEach(function (k) {
                            // copy data from meta to item if value differs
                            if (meta[k] !== undefined && item[k] != meta[k]) {
                                item[k] = meta[k];
                                if (updateSelectedFile) {
                                    this.fireDataEvent("fileMetaEdited", {id: k, value: meta[k]});
                                }
                            }
                        }, this);
                    }, this);
                }
            }, this);
            if (!found) { // new file, reload this view
                this.reload();
            }
        },

        __handleMediaRemoved: function (ev) {
            var cvs = this.getConstViewSpec();
            var data = ev.getData();
            if (data.isFolder
                || (cvs == null || sm.lang.String.isEmpty(cvs.folder))
                || data.path.indexOf(cvs.folder + '/') !== 0) {
                return;
            }
            var found = false;
            this.__table.getTableModel().iterateCachedRows(function (offset, item) {
                if (data.id == item.id) {
                    found = true;
                }
            });
            if (found) { // removed file from visible list
                this.reload();
            }
        },

        __handleFormUploadFiles: function (ev) {
            var input = ev.target;
            input.onchange = null;
            this.__uploadFiles(input.files);
        },

        __handleDropFiles: function (ev) {
            ev.stopPropagation();
            ev.preventDefault();
            this.__uploadFiles(ev.dataTransfer.files);
            this.__dropZone.hide();
        },

        __uploadFiles: function (files, cb, self) {
            if (files == null) {
                return;
            }
            var fcnt = 0;
            while (files[fcnt]) fcnt++;
            if (fcnt === 0) { //nothing to upload
                return;
            }
            var path = this.__getParentPath();
            var dlg = new sm.ui.upload.FileUploadProgressDlg(function (f) {
                return ncms.Application.ACT.getRestUrl("media.upload", path.concat(f.name));
            }, files);
            dlg.addListenerOnce("completed", function () {
                dlg.close();
                this.reload();
                if (cb != null) {
                    cb.call(self);
                }
            }, this);
            dlg.open();
        },

        __getParentPath: function () {
            function getPageLocalFolderPath(pid) {
                pid = pid.toString();
                var ret = [];
                ret.push("pages");
                for (var i = 0, l = pid.length; i < l; ++i) {
                    if (i % 3 == 0) {
                        ret.push("/");
                    }
                    ret.push(pid.charAt(i));
                }
                return ret.join("").split("/");
            }

            if (this.getInpage()) {
                // we are in in-page mode
                return getPageLocalFolderPath(this.__opts["pageSpec"]["id"])
            } else {
                return (this.getItem() != null) ? this.getItem()["path"] : [];
            }
        },

        __ensureUploadControls: function () {
            if (!this.__allowModify) { //we are in read-only mode
                return;
            }
            var el = this.getContentElement().getDomElement();
            if (el.ondrop == this.__dropFun) {
                return;
            }
            el.ondrop = this.__dropFun;
        },

        __previewFile: function (ev) {
            var f = this.__table.getSelectedFile();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = (f.folder + f.name).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.file", path);
            qx.bom.Window.open(url + "?inline=true", "NCMS:Media");
        },

        __downloadFile: function (ev) {
            var f = this.__table.getSelectedFile();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = (f.folder + f.name).split("/");
            var form = document.getElementById("ncms-download-form");
            form.action = ncms.Application.ACT.getRestUrl("media.file", path);
            form.submit();
        },

        __moveFiles: function () {
            var sfiles = this.__table.getSelectedFiles();
            if (sfiles.length == 0) {
                return;
            }

            var dlg = new ncms.mmgr.MediaSelectFolderDlg(
                sfiles.length == 1 ? this.tr("Move '%1' to another folder",
                                       sfiles[0].folder + sfiles[0].name) : this.tr("Move %1 files to another folder",
                                       sfiles.length)
            );

            var moveFiles = function (files, target, iter, cb, self) {
                if (files.length == 0) {
                    cb.call(self || this);
                    return;
                }

                var file = files.pop();
                var path = [].concat(file.folder.split('/').splice(0), file.name);
                var npath = [].concat(target, file.name);
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move",
                    path), "PUT", "application/json");
                req.setData(npath.join("/"));
                req.send(function () {
                    iter.call(self || this, files, target, iter, cb, self);
                }, this);
            };

            dlg.addListener("completed", function (ev) {
                var target = ev.getData();
                moveFiles.call(this, sfiles, target, moveFiles, function () {
                    this.reload();
                    dlg.close();
                }, this);
            }, this);
            dlg.open();

        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var file = this.__table.getSelectedFile();
            var isTextual = (file && ncms.Utils.isTextualContentType(file["content_type"]));
            var selectedSingle = (file && this.__table.getSelectionModel().getSelectedCount() == 1);

            var bt = new qx.ui.menu.Button(this.tr("Upload"));
            bt.addListenerOnce("execute", this.__addFiles, this);
            menu.add(bt);

            bt = new qx.ui.menu.Button(this.tr("New file"));
            bt.addListenerOnce("execute", this.__onNewFile, this);
            menu.add(bt);

            if (file) {
                if (selectedSingle) {
                    bt = new qx.ui.menu.Button(this.tr("Preview"));
                    bt.addListener("execute", this.__previewFile, this);
                    menu.add(bt);

                    bt = new qx.ui.menu.Button(this.tr("Download"));
                    bt.addListener("execute", this.__downloadFile, this);
                    menu.add(bt);

                    if (file.id && file.name != null) {
                        bt = new qx.ui.menu.Button(this.tr("Copy public url"));
                        bt.getContentElement().setAttribute("class", "copy_button");
                        var url = ncms.Application.ACT.getRestUrl("media.public", file);
                        url = "http://<hostname>" + url;
                        bt.getContentElement().setAttribute("data-clipboard-text", url);
                        menu.add(bt);
                    }
                    if (file.folder != null && file.name != null) {
                        bt = new qx.ui.menu.Button(this.tr("Copy path"));
                        bt.getContentElement().setAttribute("class", "copy_button");
                        bt.getContentElement().setAttribute("data-clipboard-text", file.folder + file.name);
                        menu.add(bt);
                    }
                }

                if (this.__checkEditAccess(this.__table.getSelectedFiles())) {
                    menu.add(new qx.ui.menu.Separator());

                    if (isTextual && selectedSingle) {
                        bt = new qx.ui.menu.Button(this.tr("Edit"));
                        bt.addListenerOnce("execute", this.__onEdit, this);
                        menu.add(bt);
                    }

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

            if (this.__importBt && !this.__importBt.isExcluded()) {
                menu.add(new qx.ui.menu.Separator);
                bt = new qx.ui.menu.Button(this.tr("Import from media repository"));
                menu.add(bt);
                bt.addListenerOnce("execute", this.__importFile, this);
                bt.setEnabled(this.__importBt.isEnabled());
            }

            this._setupContextMenuDelegate(menu);
            return true;
        },


        /**
         * @param menu {qx.ui.menu.Menu} Context menu
         * @protected
         */
        _setupContextMenuDelegate: function (menu) {

        },

        __onEdit: function () {
            var file = this.__table.getSelectedFile();
            if (file == null) {
                return;
            }
            var canEdit = !!(file && this.__checkEditAccess([file])),
                isTextual = !!(file && ncms.Utils.isTextualContentType(file["content_type"]));
            if (canEdit && isTextual) {
                var dlg = new ncms.mmgr.MediaTextFileEditorDlg(file, {});
                dlg.open();
            }
        },

        __checkEditAccess: function (selected) {
            if (!this.__allowModify) {
                return false;
            }
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

        __updateMetaAttribute: function (spec, attr, value, oldValue) {
            var req;
            if ("name" == attr) {
                if (spec.folder == null || spec.name == null) {
                    return;
                }
                var npath = (spec.folder + spec.name).split("/");
                req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move",
                    npath.slice(0, -1).concat(oldValue)), "PUT", "application/json");
                req.setData(npath.join("/"));
            } else {
                req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.meta",
                    {"id": spec["id"]}), "POST", "application/json");
                req.setParameter(attr, value, true);
            }

            req.send(function () {
                if (this.hasListener("fileMetaEdited")) {
                    this.fireDataEvent("fileMetaEdited", {id: attr, value: value});
                }
            }, this);
        },

        __importFile: function () {
            var dlg = new ncms.mmgr.MediaSelectFileDlg(true, this.tr("Import from media repository"));
            dlg.addListener("completed", function (ev) {
                var files = ev.getData();
                var paths = [];
                files.forEach(function (f) {
                    paths.push(f["folder"] + f["name"]);
                });
                var path = this.__getParentPath();
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.copy-batch", path), "PUT");
                req.setRequestContentType("application/json");
                req.setData(JSON.stringify(paths));
                req.send(function (resp) {
                    dlg.close();
                    this.reload();
                }, this);
            }, this);
            dlg.open();
        }
    },

    destruct: function () {
        var events = ncms.Events.getInstance();
        events.removeListener("mediaUpdated", this.__handleMediaUpdated, this);
        if (this.getContentElement() != null) {
            var el = this.getContentElement().getDomElement();
            if (el != null) {
                el.ondrop = null;
                el.ondragover = null;
                el.ondragenter = null;
                el.ondragleave = null;
            }
        }
        this.__sf = null;
        this.__table = null;
        this.__dropFun = null;
        this.__rmBt = null;
        this.__mvBt = null;
        this.__importBt = null;
        this.__editBt = null;
        this.__subfoldersBt = null;
        this.__opts = null;
        this._disposeObjects("__dropZone");
    }
});