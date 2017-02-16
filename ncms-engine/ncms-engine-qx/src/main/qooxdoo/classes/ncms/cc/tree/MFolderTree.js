/**
 * Hierachical folder-like navigation tree support mixin.
 *
 * @asset(ncms/icon/22/places/folder.png)
 * @asset(ncms/icon/22/places/folder-open.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 * @asset(ncms/icon/22/state/loading.gif)
 */
qx.Mixin.define("ncms.cc.tree.MFolderTree", {

    events: {

        /**
         * DATA: var item = {
         *        "id"     : {Object} Optional Node ID
         *        "label"  : {String} Item name.
         *        "status" : {Number} (statis & 1) != 0 - it is folder, == 0 - otherwise
         *        "system" : {Number} (system & 1) != 0 - it is system folder, == 0 otherwise
         *        "path"   : {String} Path to the item (from tree root)
         *       };
         * or null if selection cleared
         */
        itemSelected: "qx.event.type.Data",

        treeLoaded: "qx.event.type.Event"
    },

    members: {

        _treeConfig: null,

        _tree: null,

        /**
         *
         * cfg:
         * {
         *   action : {String} Action name (defined in ncms.Actions) used to load childen (required),
         *
         *   delegate : {Object?null} VirtualTree delegate used to customize tree.
         *
         *   rootLabel : {String?'root'},
         *
         *   idPathSegments : {Boolean?false} If true Node IDs will be used in path segments
         *
         *   selectRootAsNull : {Boolean} If true, root selection will be fired as `null`
         *
         *   setupChildrenRequestFn : {Function?} Optional init HTTP request object on children loading,
         *
         *   keyProperty: {String?} Optional name of tree item unique key property (eg. `id` for id primary identifier)
         *
         *   iconConverter : {Function?}
         *
         *   reloadOnFolderOpen: {Boolean?false}
         * }
         */
        _initTree: function (cfg) {
            cfg = this._treeConfig = cfg || {};
            var me = this;
            var root = qx.data.marshal.Json.createModel({
                "id": null, //generic node ID
                "label": (cfg["rootLabel"] || this.tr("Root")),   // node name
                "status": 1,       // (status & 1) != 0 - it is folder, == 0 - otherwise
                "icon": "default", // icon alias
                "loaded": true,    // is loaded
                "children": []     // node children
            }, true);

            this._loadChildren(root, function () {
                var tree = this._tree = new sm.ui.tree.ExtendedVirtualTree(root, "label", "children");
                tree.setHideRoot(false);
                tree.setIconPath("icon");
                if (typeof cfg["iconConverter"] === "function") {
                    tree.setIconOptions({
                        converter: cfg["iconConverter"]
                    });
                } else {
                    tree.setIconOptions({
                        converter: function (value, model, source, target) {
                            switch (value) {
                                case "default":
                                    if (model.getChildren != null) {
                                        var fdSuffix = target.isOpen() ? "-open" : "";
                                        return "ncms/icon/22/places/folder" + fdSuffix + ".png";
                                    } else {
                                        return "icon/22/mimetypes/office-document.png";
                                    }
                                    break;
                                default:
                                    return "ncms/icon/22/state/loading.gif";
                            }
                        }
                    });
                }

                var cfgDelegate = cfg["delegate"];
                var delegate = {

                    createItem: function () {
                        if (cfgDelegate && typeof cfgDelegate["createItem"] === "function") {
                            return cfgDelegate["createItem"]();
                        } else {
                            return new sm.ui.tree.ExtendedVirtualTreeItem();
                        }
                    },

                    configureItem: function (item) {
                        item.setOpenSymbolMode("always");
                        if (cfgDelegate && typeof cfgDelegate["configureItem"] === "function") {
                            cfgDelegate["configureItem"]();
                        }
                    },

                    bindItem: function (controller, item, index) {
                        controller.bindDefaultProperties(item, index);
                        controller.bindProperty("", "open", {
                            converter: function (value, model, source, target) {
                                var open = target.isOpen();
                                if (open) {
                                    if (!value.getLoaded()) {
                                        me._loadChildren(value, function () {
                                            value.setLoaded(true);
                                        });
                                    }
                                } else if (cfg["reloadOnFolderOpen"] && typeof value.setLoaded === "function") {
                                    value.setLoaded(false);
                                }
                                return open;
                            }
                        }, item, index);

                        if (cfgDelegate && typeof cfgDelegate["bindItem"] === "function") {
                            cfgDelegate["bindItem"](controller, item, index);
                        }
                    }
                };
                if (cfgDelegate != null) { //apply delegate properties from config
                    qx.lang.Object.mergeWith(delegate, cfgDelegate, false);
                }
                tree.setDelegate(delegate);
                tree.getSelection().addListener("change", function (e) {
                    this._onSelected(e.getTarget().getItem(0));
                }, this);
                this.addTree(tree);
                this.fireEvent("treeLoaded");
            }, this);
        },

        addTree: function (tree) {
            this._add(tree);
        },
        
        resetSelection: function () {
            this._tree && this._tree.getSelection().removeAll();
        },

        _getItemPathSegments: function (item) {
            if (this._tree == null ||
                item == null ||
                item == this._tree.getModel()) {
                return [];
            }
            var cfg = this._treeConfig;
            var root = this._tree.getModel();
            var path = (cfg["idPathSegments"] === true) ? [item.getId()] : [item.getLabel()];
            var pp = this._tree.getParent(item);
            while (pp != null && pp != root) {
                path.push((cfg["idPathSegments"] === true) ? pp.getId() : pp.getLabel());
                pp = this._tree.getParent(pp);
            }
            path.reverse();
            return path;
        },

        _getItemParentId: function (item) {
            if (item == null) {
                return null;
            }
            var pp = this._tree.getParent(item);
            return pp != null ? pp.getId() : null;

        },

        _getItemParentLabel: function (item) {
            if (item == null) {
                return null;
            }
            var pp = this._tree.getParent(item);
            return pp != null ? pp.getId() : null;
        },

        _refreshNode: function (node, cb, self, opts) {
            if (cb != null && typeof cb === "object") {
                opts = cb;
                cb = null;
                self = null;
            } else {
                opts = opts || {};
            }
            if (this._tree.isNode(node)) {
                this._loadChildren(node, function () {
                    if (opts.openNode !== false) {
                        this._tree.openNodeAndParents(node);
                    }
                    if (cb != null) {
                        cb.call(self);
                    }
                    if (opts.focus) {
                        this._tree.getSelection().splice(0, 1, node);
                        this._tree.focus();
                    }
                }, this, opts);
            } else {
                if (cb) {
                    cb.call(self);
                }
                if (opts.focus) {
                    this._tree.getSelection().splice(0, 1, node);
                    this._tree.focus();
                }
            }
        },

        _onSelected: function (item) {
            var cfg = this._treeConfig;
            var data = (item == null || (cfg["selectRootAsNull"] && item == this._tree.getModel())) ? null : {
                "id": item.getId(),
                "label": item.getLabel(),
                "status": item.getStatus(),
                "path": this._getItemPathSegments(item),
                "accessMask": (item.getAccessMask != null) ? item.getAccessMask() : null

            };
            if (this.hasListener("itemSelected")) {
                this.fireDataEvent("itemSelected", data);
            }
        },

        _loadChildren: function (parent, cb, self, opts) {
            opts = opts || {};
            var cfg = this._treeConfig,
                url = ncms.Application.ACT.getRestUrl(cfg["action"], this._getItemPathSegments(parent)),
                req = new sm.io.Request(url, "GET", "application/json");

            if (typeof cfg["setupChildrenRequestFn"] === "function") {
                cfg["setupChildrenRequestFn"].call(this, req);
            }

            req.send(function (resp) {
                var data = resp.getContent(),
                    children = parent.getChildren();

                data.forEach(function (node) {
                    if (node["id"] === undefined) {
                        node["id"] = null;
                    }
                    node["icon"] = "default";
                    if ((node["status"] & 1) !== 0) { // is folder?
                        node["loaded"] = false;
                        node["children"] = [
                            {
                                label: "Loading",
                                icon: "loading"
                            }
                        ]
                    } else {
                        node["loaded"] = true;
                    }
                });

                while (true) {

                    if (cfg["keyProperty"] == null || opts.updateOnly == null) { // children full refresh
                        children.removeAll();
                        children.append(data.map(function (node) {
                            return qx.data.marshal.Json.createModel(node, true);
                        }));
                    } else { // ADD / REMOVE / UPDATE

                        var cnt = (function () {

                            var keyProperty = cfg["keyProperty"],
                                keyAccessor = "get" + qx.lang.String.firstUp(keyProperty),
                                onlyDataItem = null,
                                onlyChildItem = null,
                                ind = -1,
                                isEmpty = true;

                            children.forEach(function (c) {
                                if (typeof c[keyAccessor] === "function") {
                                    isEmpty = false;
                                    if (c[keyAccessor]() === opts.updateOnly) {
                                        onlyChildItem = c;
                                    }
                                }
                            });

                            if (isEmpty) {  // children node is semi-empty                             // FULL REFRESH
                                opts.updateOnly = null;
                                return true;
                            }

                            data.forEach(function (c) {
                                if ((c[keyProperty] != null) && c[keyProperty] === opts.updateOnly) {
                                    onlyDataItem = c;
                                }
                            });

                            if (onlyChildItem != null && onlyDataItem == null) {                         // REMOVE
                                children.remove(onlyChildItem);
                            } else if (onlyDataItem != null && onlyChildItem == null) {                  // ADD
                                ind = data.indexOf(onlyDataItem);
                                children.splice(ind, 0, qx.data.marshal.Json.createModel(onlyDataItem, true));
                            } else {                                                                     // UPDATE
                                ind = children.indexOf(onlyChildItem);
                                if (ind !== -1) {
                                    //todo do not create a new node and update existing
                                    children.splice(ind, 1, qx.data.marshal.Json.createModel(onlyDataItem, true));
                                }
                            }
                        })();

                        if (cnt === true) {
                            continue;
                        }
                    }
                    break;
                }
                if (cb != null) {
                    cb.call(self);
                }
            }, this);
        }
    },

    destruct: function () {
        this._treeConfig = null;
        this._tree = null;
    }
});
