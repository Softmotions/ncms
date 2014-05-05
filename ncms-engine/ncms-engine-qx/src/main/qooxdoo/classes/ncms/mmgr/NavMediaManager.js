/**
 * Media folders tree
 *
 * @asset(qx/icon/${qx.icontheme}/22/places/folder.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 * @asset(ncms/icon/22/state/loading.gif)
 */
qx.Class.define("ncms.mmgr.NavMediaManager", {
    extend : qx.ui.core.Widget,

    statics : {
        MMF_EDITOR_CLAZZ : "ncms.mmgr.MediaFolderEditor"
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());

        //Register media folder editor
        var eclazz = ncms.mmgr.NavMediaManager.MMF_EDITOR_CLAZZ;
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
                tree.setHideRoot(true);
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
                    bindItem : function(controller, item, index) {
                        controller.bindDefaultProperties(item, index);
                        controller.bindProperty("", "open", {
                            converter : function(value, model, source, target) {
                                var isOpen = target.isOpen();
                                if (isOpen && !value.getLoaded()) {
                                    me._loadChildren(value, function() {
                                        value.setLoaded(true);
                                    });
                                }
                                return isOpen;
                            }
                        }, item, index);
                    }
                };
                tree.setDelegate(delegate);
                this._add(tree);
            }, this);
        },

        _loadChildren : function(parent, cb, self) {
            var url;
            if (this.__tree != null) {
                var path = [parent.getLabel()];
                var pp = this.__tree.getParent(parent);
                while (pp != null) {
                    path.push(pp.getLabel());
                    pp = this.__tree.getParent(pp);
                }
                path.reverse();
                url = ncms.Application.ACT.getRestUrl("media.list", path);
            } else {
                url = ncms.Application.ACT.getRestUrl("media.list");
            }
            var req = new sm.io.Request(url, "GET", "application/json");
            req.send(function(resp) {
                var data = resp.getContent();
                parent.getChildren().removeAll();
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
                    parent.getChildren().push(qx.data.marshal.Json.createModel(node, true));
                }
                if (cb != null) {
                    cb.call(self);
                }
            }, this);
        },

        createRandomData : function(parent) {
            var items = parseInt(Math.random() * 50);
            for (var i = 0; i < items; i++) {
                var node = {
                    label : "Item " + this.count++,
                    icon : "default",
                    loaded : true
                };
                if (Math.random() > 0.3) {
                    node["loaded"] = false;
                    node["children"] = [
                        {
                            label : "Loading",
                            icon : "loading"
                        }
                    ];
                }
                parent.getChildren().push(qx.data.marshal.Json.createModel(node, true));
            }
        }
    },

    destruct : function() {
        this.__tree = null;
        //this._disposeObjects("__field_name");
    }
});