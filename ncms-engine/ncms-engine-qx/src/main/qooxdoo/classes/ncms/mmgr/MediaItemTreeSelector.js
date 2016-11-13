/**
 * Media folder/file tree-selector.
 *
 * @asset(ncms/icon/22/places/folder.png)
 * @asset(ncms/icon/22/places/folder-open.png)
 * @asset(ncms/icon/22/places/system-folder.png)
 * @asset(ncms/icon/22/places/system-folder-open.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 * @asset(ncms/icon/22/state/loading.gif)
 */
qx.Class.define("ncms.mmgr.MediaItemTreeSelector", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.tree.MFolderTree, ncms.cc.MCommands],

    construct: function (allowModify) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());

        this._initTree({
            reloadOnFolderOpen: true,
            action: "media.folders",
            rootLabel: this.tr("Files"),
            iconConverter: this.__treeIconConverter.bind(this)
        });

        if (allowModify) {
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            // Init shortcuts
            this._registerCommand(
                new sm.ui.core.ExtendedCommand("Delete"),
                this.__onDelete, this);
            this._registerCommand(
                new sm.ui.core.ExtendedCommand("F2"),
                this.__onRename, this);
            this._registerCommand(
                new sm.ui.core.ExtendedCommand("F6"),
                this.__onMove, this);
            this._registerCommand(
                new sm.ui.core.ExtendedCommand("Alt+Insert"),
                this.__onNewFolder, this);
        }

        var events = ncms.Events.getInstance();
        events.addListener("mediaUpdated", this.__onMediaUpdatedRemoved, this);
        events.addListener("mediaRemoved", this.__onMediaUpdatedRemoved, this);

        this.addListenerOnce("treeLoaded", function () {
            this._registerCommandFocusWidget(this._tree);
        }, this);
    },

    members: {

        __onMediaUpdatedRemoved: function (ev) {
            var data = ev.getData();
            if (data.hints["app"] === ncms.Application.UUID || !data["isFolder"]) {
                return;
            }
            // todo implement it
        },

        __treeIconConverter: function (value, model, source, target) {
            switch (value) {
                case "default":
                    if (model.getChildren != null) {
                        var fdPreffix = model.getSystem && model.getSystem() == 1 ? "system-" : "";
                        var fdSuffix = target.isOpen() ? "-open" : "";
                        return "ncms/icon/22/places/" + fdPreffix + "folder" + fdSuffix + ".png";
                    } else {
                        return "icon/22/mimetypes/office-document.png";
                    }
                    break;
                default:
                    return "ncms/icon/22/state/loading.gif";
            }
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt;
            var tree = this._tree;
            var root = tree.getModel();
            var sel = tree.getSelection().getItem(0);

            if (sel != null) {
                if (sel != root) {
                    bt = new qx.ui.menu.Button(this.tr("New subfolder"));
                    bt.addListenerOnce("execute", this.__onNewFolder, this);
                    menu.add(bt);
                }
            }

            bt = new qx.ui.menu.Button(this.tr("New top-level folder"));
            bt.addListenerOnce("execute", this.__onNewRootFolder, this);
            menu.add(bt);

            bt = new qx.ui.menu.Button(this.tr("New file"));
            bt.addListenerOnce("execute", this.__onNewFile, this);
            menu.add(bt);

            if (sel != null) {
                if (sel != root) {
                    menu.add(new qx.ui.menu.Separator());

                    bt = new qx.ui.menu.Button(this.tr("Rename"));
                    bt.addListenerOnce("execute", this.__onRename, this);
                    menu.add(bt);

                    bt = new qx.ui.menu.Button(this.tr("Move to another folder"));
                    bt.addListenerOnce("execute", this.__onMove, this);
                    menu.add(bt);

                    bt = new qx.ui.menu.Button(this.tr("Delete"));
                    bt.addListenerOnce("execute", this.__onDelete, this);
                    menu.add(bt);
                }
            }

            menu.add(new qx.ui.menu.Separator());
            bt = new qx.ui.menu.Button(this.tr("Refresh"));
            bt.addListenerOnce("execute", this.__onRefresh, this);
            menu.add(bt);
        },

        __onRefresh: function () {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            this._refreshNode2(item);
        },

        __onDelete: function () {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var parent = this._tree.getParent(item) || this._tree.getModel();
            var path = this._getItemPathSegments(item);
            ncms.Application.confirm(this.tr("Are you sure to remove folder: %1", path.join("/")), function (yes) {
                if (!yes) return;
                var url = ncms.Application.ACT.getRestUrl("media.delete", path);
                var req = new sm.io.Request(url, "DELETE", "application/json");
                req.send(function () {
                    this._refreshNode(parent, {
                        focus: true
                    });
                }, this);
            }, this);
        },

        __onRename: function (ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var path = this._getItemPathSegments(item);
            var dlg = new ncms.mmgr.MediaItemRenameDlg(path, item.getLabel());
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                var data = ev.getData();
                item.setLoaded(false);
                item.setLabel(data[0]);
                this._onSelected(item);
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(this._tree, false);
            }
            dlg.open();
        },

        __onMove: function () {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var path = this._getItemPathSegments(item);
            var parent = this._tree.getParent(item) || this._tree.getModel();

            var dlg = new ncms.mmgr.MediaSelectFolderDlg(
                this.tr("Move '%1' to another folder", path.join("/"))
            );
            dlg.addListener("completed", function (ev) {
                var target = ev.getData();
                var npath = [].concat(target, item.getLabel());
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move", path),
                    "PUT", "application/json");
                req.setData(npath.join("/"));
                req.send(function () {
                    dlg.close();
                    this._refreshNode(parent, function () {
                        target = this._tree.findCachedNodeByPath(target, "label");
                        if (target != null) {
                            this._refreshNode(target);
                        }
                    }, this);
                }, this);
            }, this);
            dlg.open();
        },

        __onNewFolder: function (ev) {
            this.__newFolder(ev, this._tree.getSelection().getItem(0) || this._tree.getModel());
        },

        __onNewRootFolder: function (ev) {
            this.__newFolder(ev, this._tree.getModel());
        },

        __onNewFile: function (ev) {
            this.__newFile(ev, this._tree.getSelection().getItem(0) || this._tree.getModel());
        },

        __newFolder: function (ev, parent) {
            var path = this._getItemPathSegments(parent);
            var dlg = new ncms.mmgr.MediaFolderNewDlg(path);
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this._refreshNode2(parent);
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(this._tree, false);
            }
            dlg.open();
        },

        __newFile: function (ev, parent) {
            var path = this._getItemPathSegments(parent);
            var dlg = new ncms.mmgr.MediaFileNewDlg(path);
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this._refreshNode2(parent);
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        _refreshNode2: function (refreshItem) {
            var root = this._tree.getModel();
            var selectedItem = this._tree.getSelection().getItem(0);
            this._refreshNode(refreshItem, function () {
                if (selectedItem === refreshItem) {
                    this._onSelected(refreshItem)
                }
            }, this);
        }
    },

    destruct: function () {
        //this._disposeObjects("__field_name");
        var events = ncms.Events.getInstance();
        events.removeListener("mediaUpdated", this.__onMediaUpdatedRemoved, this);
        events.removeListener("mediaRemoved", this.__onMediaUpdatedRemoved, this);
    }
});