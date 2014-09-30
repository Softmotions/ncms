/**
 * Pages tree selector.
 *
 * @asset(ncms/icon/22/misc/document.png)
 * @asset(ncms/icon/22/misc/document-exclamation.png)
 * @asset(ncms/icon/22/places/folder-exclamation.png)
 * @asset(ncms/icon/22/places/folder-exclamation-open.png)
 */
qx.Class.define("ncms.pgs.PagesTreeSelector", {
    extend : qx.ui.core.Widget,
    include : [ ncms.cc.tree.MFolderTree ],

    /**
     * @param allowModify {Boolean?false} Allow CRUD operations on pages
     * @param options {Map?} Options:
     *                <code>
     *                    {
     *                      foldersOnly : {Boolean?false} //Show only folders
     *                      accessAll : {String?} //Optional access all page security restriction
     *                    }
     *                </code>
     */
    construct : function(allowModify, options) {
        this.__options = options || {};
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        var me = this;
        this._initTree(
                {
                    action : "pages.layer",
                    idPathSegments : true,
                    rootLabel : this.tr("Pages"),
                    selectRootAsNull : true,
                    setupChildrenRequestFn : this.__setupChildrenRequest,
                    iconConverter : this.__treeIconConverter.bind(this),
                    delegate : {
                        bindItem : function(controller, item, index) {
                            controller.bindProperty("", "model", {
                                converter : function(value, model, source, target) {
                                    me.__configureItem(value, target);
                                    return value;
                                }
                            }, item, index);
                        }
                    }
                });
        if (allowModify) {
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
        }
        ncms.Events.getInstance().addListener("pageChangePublished", this.__onPagePublished, this);
    },

    members : {

        __options : null,

        getTree : function() {
            return this._tree;
        },

        __configureItem : function(model, item) {
            var checkAm = this.__options["accessAll"];
            if (checkAm == null) {
                return;
            }
            var am = (typeof model.getAccessMask === "function") ? model.getAccessMask() : null;
            if (am == null || typeof am !== "string") {
                return;
            }
            var ok = ncms.Utils.checkAccessAll(am, checkAm);
            if (ok) {
                item.removeState("locked");
            } else {
                item.addState("locked");
            }
        },

        __onPagePublished : function(ev) {
            var data = ev.getData();
            var published = data["published"];
            var id = data["id"];
            this._tree.iterateOverCachedNodes(function(node) {
                if (node.getId() === id && node.getStatus != null) {
                    var status = node.getStatus();
                    if (published) {
                        status &= ~(1 << 1);

                    } else {
                        status |= (1 << 1);
                    }
                    node.setStatus(status);
                    node.setIcon(published ? "published" : "unpublished");
                    return true;
                }
            });
        },

        __treeIconConverter : function(value, model, source, target) {
            var statusPrefix = "";
            if (model.getStatus && (model.getStatus() & (1 << 1)) != 0) {
                statusPrefix = "-exclamation";
            }
            switch (value) {
                case "published":
                case "unpublished":
                case "default":
                    if (model.getChildren != null) {
                        var fdSuffix = target.isOpen() ? "-open" : "";
                        return "ncms/icon/22/places/folder" + statusPrefix + fdSuffix + ".png";
                    } else {
                        return "ncms/icon/22/misc/document" + statusPrefix + ".png";
                    }
                    break;
                default:
                    return "ncms/icon/22/state/loading.gif";
            }
        },

        __setupChildrenRequest : function(req) {
            if (this.__options["foldersOnly"]) {
                req.setParameter("foldersOnly", "true");
            }
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var tree = this._tree;
            var root = tree.getModel();
            var sel = tree.getSelection().getItem(0);
            var bt;

            var parent = this.__calcFirstFolderParent(sel);
            if (ncms.Application.userInRoles("admin.structure") || (parent != root && parent.getAccessMask().indexOf("w") != -1)) {
                bt = new qx.ui.menu.Button(this.tr("New"));
                bt.addListenerOnce("execute", this.__onNewPage, this);
                menu.add(bt);
            }

            if (sel != null && sel != root && (sel.getAccessMask().indexOf("d") != -1)) {
                menu.add(new qx.ui.menu.Separator());

                bt = new qx.ui.menu.Button(this.tr("Change/Rename"));
                bt.addListenerOnce("execute", this.__onChangeOrRenamePage, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Move"));
                bt.addListenerOnce("execute", this.__onMovePage, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Delete"));
                bt.addListenerOnce("execute", this.__onDeletePage, this);
                menu.add(bt);
            }
        },

        __onMovePage : function(ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            //item={"id":5,"guid":"9eb264b3e1f73b1319224360b5d0db02",
            // "label":"Привет","description":"Привет","status":0,"type":"page",
            // "options":null,"accessMask":"wnd","icon":"default","loaded":true}
            var parent = this._tree.getParent(item) || this._tree.getModel();
            var dlg = new ncms.pgs.PagesSelectorDlg(
                    this.tr("Choose the target container page"),
                    false,
                    {
                        foldersOnly : true,
                        allowRootSelection : true
                    });
            dlg.addListener("completed", function(ev) {
                var target = ev.getData();
                //{"id":7,"name":"Test2","idPath":[7],"labelPath":["Test2"],"guidPath":["dd67c81082248a0241937f0ebbbd01d3"]}
                var req = new sm.io.Request(ncms.Application.ACT.getUrl("pages.move"), "PUT");
                req.setRequestContentType("application/json");
                req.setData(JSON.stringify({
                    src : item.getId(),
                    tgt : target != null ? target["id"] : 0
                }));
                req.send(function() {
                    dlg.close();
                    this._refreshNode(parent, function() {
                        if (target == null) { //refresh root
                            this._refreshNode(this._tree.getModel());
                            return;
                        }
                        this._tree.iterateOverCachedNodes(function(node) {
                            if (node.getId() === target["id"]) {
                                if (parent != node) {
                                    this._refreshNode(node);
                                }
                                return true; //stop iteration
                            }
                        }, this);
                    }, this);
                }, this);
            }, this);
            dlg.open();
        },

        __onChangeOrRenamePage : function(ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var parent = this._tree.getParent(item) || this._tree.getModel();
            var dlg = new ncms.pgs.PageChangeOrRenameDlg({
                id : item.getId(),
                label : item.getLabel(),
                status : item.getStatus()
            });
            dlg.addListener("completed", function(ev) {
                this._refreshNode(parent);
                dlg.close();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __onNewPage : function(ev) {
            var parent = this.__calcFirstFolderParent(this._tree.getSelection().getItem(0));
            var parentId = parent.getId();
            var dlg = new ncms.pgs.PageNewDlg(parentId);
            dlg.addListener("completed", function(ev) {
                this._refreshNode(parent);
                dlg.close();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __onDeletePage : function(ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var parent = this._tree.getParent(item) || this._tree.getModel();
            var label = item.getLabel();
            ncms.Application.confirm(this.tr("Are you sure to remove page: %1", label), function(yes) {
                if (!yes) return;
                var url = ncms.Application.ACT.getRestUrl("pages.delete", {id : item.getId()});
                var req = new sm.io.Request(url, "DELETE", "application/json");
                req.send(function(resp) {
                    var ret = resp.getContent();
                    //todo
                    qx.log.Logger.info("ret=" + JSON.stringify(ret));
                    this._refreshNode(parent);
                }, this);
            }, this);
        },

        __calcFirstFolderParent : function(item) {
            var parent = item;
            while (parent && (parent.getStatus() & 1) === 0) {
                parent = this._tree.getParent(parent);
            }
            return parent || this._tree.getModel();
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
        ncms.Events.getInstance().removeListener("pageChangePublished", this.__onPagePublished, this);
    }
});