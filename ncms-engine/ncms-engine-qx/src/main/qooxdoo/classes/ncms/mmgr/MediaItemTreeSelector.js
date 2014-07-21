/**
 * Media folder/file tree-selector.
 */
qx.Class.define("ncms.mmgr.MediaItemTreeSelector", {
    extend : qx.ui.core.Widget,
    include : [ ncms.cc.tree.MFolderTree ],

    construct : function(allowModify) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this._initTree({
            "action" : "media.folders",
            "rootLabel" : this.tr("Files")
        });
        if (allowModify) {
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
        }
    },

    members : {

        __beforeContextmenuOpen : function(ev) {
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

        __onRefresh : function(ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            this._refreshNode(item);
        },

        __onDelete : function(ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var parent = this._tree.getParent(item) || this._tree.getModel();
            var path = this._getItemPathSegments(item);
            ncms.Application.confirm(this.tr("Are you sure to remove folder: %1", path.join("/")), function(yes) {
                if (!yes) return;
                var url = ncms.Application.ACT.getRestUrl("media.delete", path);
                var req = new sm.io.Request(url, "DELETE", "application/json");
                req.send(function(resp) {
                    this._refreshNode(parent);
                }, this);
            }, this);
        },

        __onRename : function(ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var path = this._getItemPathSegments(item);
            var d = new ncms.mmgr.MediaItemRenameDlg(path, item.getLabel());
            d.setPosition("bottom-right");
            d.addListener("completed", function(ev) {
                d.destroy();
                var data = ev.getData();
                item.setLoaded(false);
                item.setLabel(data[0]);
                this._onSelected(item);
            }, this);
            d.placeToWidget(ev.getTarget(), false);
            d.show();
        },

        __onMove : function(ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var path = this._getItemPathSegments(item);
            var parent = this._tree.getParent(item) || this._tree.getModel();

            var d = new ncms.mmgr.MediaSelectFolderDlg(
                    this.tr("Move '%1' to another folder", path.join("/"))
            );
            d.addListener("completed", function(ev) {
                var target = ev.getData();
                var npath = [].concat(target, item.getLabel());
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move", path),
                        "PUT", "application/json");
                req.setData(npath.join("/"));
                req.send(function() {
                    d.close();
                    this._refreshNode(parent, function() {
                        target = this._tree.findCachedNodeByPath(target, "label");
                        if (target != null) {
                            this._refreshNode(target);
                        }
                    }, this);
                }, this);
            }, this);
            d.show();
        },

        __onNewFolder : function(ev) {
            this.__newFolder(ev, this._tree.getSelection().getItem(0) || this._tree.getModel());
        },

        __onNewRootFolder : function(ev) {
            this.__newFolder(ev, this._tree.getModel());
        },

        __newFolder : function(ev, parent) {
            var path = this._getItemPathSegments(parent);
            var d = new ncms.mmgr.MediaFolderNewDlg(path);
            d.setPosition("bottom-right");
            d.addListener("completed", function(ev) {
                d.close();
                this._refreshNode(parent);
            }, this);
            d.placeToWidget(ev.getTarget(), false);
            d.show();
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});