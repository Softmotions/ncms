/**
 * Media folders tree
 *
 * @asset(qx/icon/${qx.icontheme}/22/places/folder.png)
 * @asset(qx/icon/${qx.icontheme}/22/places/folder-open.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 * @asset(ncms/icon/22/state/loading.gif)
 */
qx.Class.define("ncms.mmgr.MediaNav", {
    extend : qx.ui.core.Widget,
    include : [ ncms.mmgr.MMediaItemTree ],

    statics : {
        MMF_EDITOR_CLAZZ : "ncms.mmgr.MediaFolderEditor"
    },


    construct : function(opts) {
        this.base(arguments, opts);
        this._setLayout(new qx.ui.layout.Grow());

        //Register media folder editor
        var eclazz = ncms.mmgr.MediaNav.MMF_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.mmgr.MediaFolderEditor();
        }, null, this);

        this.addListener("disappear", function() {
            //Navigation side is inactive so hide mmfolder editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
        }, this);
        this.addListener("appear", function() {
            if (app.getActiveWSAID() != eclazz) {
                app.showWSA(eclazz);
            }
        }, this);

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

        this.addListener("itemSelected", function(ev) {
            var data = ev.getData();
            if (data == null) {
                app.showDefaultWSA();
            } else {
                app.getWSA(eclazz).setItem(data);
                app.showWSA(eclazz);
            }
        })
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
            d.addListenerOnce("completed", function(ev) {
                d.hide();
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
            var d = new ncms.mmgr.MediaSelectFolderDlg();
            d.addListener("completed", function(ev) {
                var npath = ev.getData();
                npath.push(item.getLabel());
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.move", path),
                        "PUT", "application/json");
                req.setData(npath.join("/"));
                req.send(function(resp) {
                    d.close();
                    this._refreshNode(parent, function() {
                        //todo refresh target !!! !!! !!!
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
            d.addListenerOnce("completed", function(ev) {
                d.hide();
                this._refreshNode(parent);
            }, this);
            d.placeToWidget(ev.getTarget(), false);
            d.show();
        }
    },

    destruct : function() {
    }
});