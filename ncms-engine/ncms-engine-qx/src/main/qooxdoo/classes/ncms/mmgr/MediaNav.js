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

    statics : {
        MMF_EDITOR_CLAZZ : "ncms.mmgr.MediaFolderEditor"
    },

    events : {

        /**
         * DATA: var item = {
         *        "label"  : {String} Item name.
         *        "status" : {Number} Item status. (1 - folder, 0 - file)
         *        "path"   : {String} Path to the item (from tree root)
         *       };
         * or null if selection cleared
         */
        itemSelected : "qx.event.type.Data"
    },

    properties : {

        /**
         * List folders only
         */
        foldersOnly : {
            check : "Boolean",
            init : true,
            nullable : false
        }
    },

    construct : function(opts) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());

        opts = opts || {};
        if (opts["foldersOnly"] != null) {
            this.setFoldersOnly(!!opts["foldersOnly"]);
        }

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

        this.__initTree();

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
    },

    members : {

        __tree : null,

        __initTree : function() {
            var me = this;
            var root = qx.data.marshal.Json.createModel({
                "label" : "root",   // node name
                "status" : 1,       // 1 - it is folder, 0 - otherwise
                "icon" : "default", // icon alias
                "loaded" : true,    // is loaded
                "children" : []     // node children
            }, true);

            this._loadChildren(root, function() {
                var tree = this.__tree = new qx.ui.tree.VirtualTree(root, "label", "children");
                tree.setHideRoot(false);
                tree.setIconPath("icon");
                tree.setIconOptions({
                    converter : function(value, model) {
                        switch (value) {
                            case "default":
                                if (model.getChildren != null) {
                                    return "icon/22/places/folder.png";
                                } else {
                                    return "icon/22/mimetypes/office-document.png";
                                }
                                break;
                            default:
                                return "ncms/icon/22/state/loading.gif";
                        }
                    }
                });

                var delegate = {

                    createItem : function() {
                        return new sm.ui.tree.ExtendedVirtualTreeItem();
                    },

                    configureItem : function(item) {
                        item.setOpenSymbolMode("always");
                        item.setIconOpened("icon/22/places/folder-open.png");
                    },

                    bindItem : function(controller, item, index) {
                        controller.bindDefaultProperties(item, index);
                        controller.bindProperty("", "open", {
                            converter : function(value, model, source, target) {
                                var open = target.isOpen();
                                if (open && !value.getLoaded()) {
                                    me._loadChildren(value, function() {
                                        value.setLoaded(true);
                                    });
                                }
                                return open;
                            }
                        }, item, index);
                    }
                };
                tree.setDelegate(delegate);
                tree.getSelection().addListener("change", function(e) {
                    this.__onSelected(e.getTarget().getItem(0));
                }, this);

                this._add(tree);
            }, this);
        },

        __onSelected : function(item) {
            var app = ncms.Application.INSTANCE;
            var eclazz = ncms.mmgr.MediaNav.MMF_EDITOR_CLAZZ;
            if (item == null) {
                app.showDefaultWSA();
                if (this.hasListener("itemSelected")) {
                    this.fireDataEvent("itemSelected", null);
                }
                return;
            }
            var data = {
                "label" : item.getLabel(),
                "status" : item.getStatus(),
                "path" : this._getItemPathSegments(item)

            };
            app.getWSA(eclazz).setItem(data);
            app.showWSA(eclazz);
            if (this.hasListener("itemSelected")) {
                this.fireDataEvent("itemSelected", data);
            }
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt;
            var tree = this.__tree;
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

                    bt = new qx.ui.menu.Button(this.tr("Delete"));
                    bt.addListenerOnce("execute", this.__onDelete, this);
                    menu.add(bt);
                }
            }
        },

        __onDelete : function(ev) {
            var item = this.__tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var parent = this.__tree.getParent(item) || this.__tree.getModel();
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
            var item = this.__tree.getSelection().getItem(0);
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
                this.__onSelected(item);
            }, this);
            d.placeToWidget(ev.getTarget(), false);
            d.show();
        },

        __onNewFolder : function(ev) {
            this.__newFolder(ev, this.__tree.getSelection().getItem(0) || this.__tree.getModel());
        },

        __onNewRootFolder : function(ev) {
            this.__newFolder(ev, this.__tree.getModel());
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
        },


        _refreshNode : function(node) {
            if (this.__tree.isNode(node)) {
                this._loadChildren(node, function() {
                    this.__tree.openNode(node);
                }, this);
            }
        },

        _loadChildren : function(parent, cb, self) {
            var url = ncms.Application.ACT.getRestUrl("media.folders", this._getItemPathSegments(parent));
            var req = new sm.io.Request(url, "GET", "application/json");
            req.send(function(resp) {
                var data = resp.getContent();
                var children = parent.getChildren();
                children.removeAll();
                for (var i = 0, l = data.length; i < l; ++i) {
                    var node = data[i];
                    node["icon"] = "default";
                    if (node["status"] == 1) {
                        node["loaded"] = false;
                        node["children"] = [
                            {
                                label : "Loading",
                                icon : "loading"
                            }
                        ]
                    } else {
                        node["loaded"] = true;
                    }
                    children.push(qx.data.marshal.Json.createModel(node, true));
                }
                if (cb != null) {
                    cb.call(self);
                }
            }, this);
        },

        _getItemPathSegments : function(item) {
            if (this.__tree == null ||
                    item == null ||
                    item == this.__tree.getModel()) {
                return [];
            }
            var root = this.__tree.getModel();
            var path = [item.getLabel()];
            var pp = this.__tree.getParent(item);
            while (pp != null && pp != root) {
                path.push(pp.getLabel());
                pp = this.__tree.getParent(pp);
            }
            path.reverse();
            return path;
        }
    },

    destruct : function() {
        this.__tree = null;
        //this._disposeObjects("__field_name");
    }
});