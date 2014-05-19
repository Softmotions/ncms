/**
 * Media manager directories selector.
 *
 * @asset(qx/icon/${qx.icontheme}/22/places/folder.png)
 * @asset(qx/icon/${qx.icontheme}/22/places/folder-open.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 * @asset(ncms/icon/22/state/loading.gif)
 */
qx.Mixin.define("ncms.mmgr.MMediaItemTree", {

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
    },

    construct : function() {
        this._initTree();
    },

    members : {

        _tree : null,

        _initTree : function() {
            var me = this;
            var root = qx.data.marshal.Json.createModel({
                "label" : "root",   // node name
                "status" : 1,       // 1 - it is folder, 0 - otherwise
                "icon" : "default", // icon alias
                "loaded" : true,    // is loaded
                "children" : []     // node children
            }, true);

            this._loadChildren(root, function() {
                var tree = this._tree = new sm.ui.tree.ExtendedVirtualTree(root, "label", "children");
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
                    this._onSelected(e.getTarget().getItem(0));
                }, this);

                this.addTree(tree);
            }, this);
        },

        addTree : function(tree) {
            this._add(tree);
        },

        _getItemPathSegments : function(item) {
            if (this._tree == null ||
                    item == null ||
                    item == this._tree.getModel()) {
                return [];
            }
            var root = this._tree.getModel();
            var path = [item.getLabel()];
            var pp = this._tree.getParent(item);
            while (pp != null && pp != root) {
                path.push(pp.getLabel());
                pp = this._tree.getParent(pp);
            }
            path.reverse();
            return path;
        },

        _refreshNode : function(node, cb, self) {
            if (this._tree.isNode(node)) {
                this._loadChildren(node, function() {
                    this._tree.openNodeAndParents(node);
                    if (cb != null) {
                        cb.call(self);
                    }
                }, this);
            } else {
                if (cb) {
                    cb.call(self);
                }
            }
        },

        _onSelected : function(item) {
            var data = (item == null) ? null : {
                "label" : item.getLabel(),
                "status" : item.getStatus(),
                "path" : this._getItemPathSegments(item)

            };
            if (this.hasListener("itemSelected")) {
                this.fireDataEvent("itemSelected", data);
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
        }
    },

    destruct : function() {
        this._tree = null;
        //this._disposeObjects("__field_name");                                
    }
});