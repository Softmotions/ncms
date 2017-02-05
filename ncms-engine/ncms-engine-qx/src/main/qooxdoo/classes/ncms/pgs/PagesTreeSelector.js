/**
 * Pages tree selector.
 *
 * @asset(ncms/icon/22/misc/document.png)
 * @asset(ncms/icon/22/misc/document-exclamation.png)
 * @asset(ncms/icon/22/places/folder-exclamation.png)
 * @asset(ncms/icon/22/places/folder-exclamation-open.png)
 */
qx.Class.define("ncms.pgs.PagesTreeSelector", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.tree.MFolderTree, ncms.cc.MCommands],

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
    construct: function (allowModify, options) {
        this.__options = options || {};
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        var me = this;
        this._initTree(
            {
                keyProperty: "id",
                action: "pages.layer",
                idPathSegments: true,
                rootLabel: this.tr("Pages"),
                selectRootAsNull: true,
                reloadOnFolderOpen: true,
                setupChildrenRequestFn: this.__setupChildrenRequest,
                iconConverter: this.__treeIconConverter.bind(this),
                delegate: {
                    bindItem: function (controller, item, index) {
                        controller.bindProperty("", "model", {
                            converter: function (value, model, source, target) {
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

        var events = ncms.Events.getInstance();
        events.addListener("pageChangePublished", this.__onPagePublished, this);
        events.addListener("pageCreated", this.__onPageCreated, this);
        events.addListener("pageRemoved", this.__onPageEditedRemoved, this);
        events.addListener("pageEdited", this.__onPageEditedRemoved, this);

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onDeletePage, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("F2"),
            this.__onChangeOrRenamePage, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("F6"),
            this.__onMovePage, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNewPage, this);
        this.addListenerOnce("treeLoaded", function () {
            this._registerCommandFocusWidget(this._tree);
        }, this);
    },

    members: {

        __options: null,

        getTree: function () {
            return this._tree;
        },

        __configureItem: function (model, item) {
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

        __onPageEditedRemoved: function (ev) {
            var parent;
            var data = ev.getData();
            if (data.hints["app"] === ncms.Application.UUID) {
                return;
            }
            var item = null;
            this._tree.iterateOverCachedNodes(function (node) {
                if (node.getId() === data.id) {
                    item = node;
                    return true;
                }
            }, this);
            if (item != null) {
                parent = this._tree.getParent(item) || this._tree.getModel();
                this._refreshNode(parent, {
                    openNode: false,
                    updateOnly: data.id
                });
            }
            if (data.hints["moveTargetId"] != null) {
                var moveTargetId = data.hints["moveTargetId"];
                parent = null;
                if (moveTargetId == 0) {
                    parent = this._tree.getModel();
                } else {
                    this._tree.iterateOverCachedNodes(function (node) {
                        if (node.getId() === moveTargetId) {
                            parent = node;
                            return true;
                        }
                    }, this);
                }
                parent && this._refreshNode(parent, {
                    openNode: false,
                    updateOnly: data.id
                });
            }
        },

        __onPageCreated: function (ev) {
            var data = ev.getData();
            if (data.hints["app"] === ncms.Application.UUID) {
                return;
            }
            var parent = null;
            if (data.navParentId == null) { // Page added to the root
                parent = this._tree.getModel();
            }
            if (parent == null) {
                this._tree.iterateOverCachedNodes(function (item) {
                    if (item.getId() === data.navParentId) {
                        parent = item;
                        return true;
                    }
                }, this);
            }
            parent && this._refreshNode(parent, {
                openNode: false,
                updateOnly: data.id
            });
        },

        __onPagePublished: function (ev) {
            var data = ev.getData();
            var published = data["published"];
            var id = data["id"];
            this._tree.iterateOverCachedNodes(function (node) {
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

        __treeIconConverter: function (value, model, source, target) {
            var statusPrefix = "",
                status = (model.getStatus != null) ? model.getStatus() : 0;
            if (
                (status & (1 << 1)) != 0 /* not published*/
                && ((status & 1) == 0 /* not a folder */
                || (status & (1 << 2)) != 0 /* folder has parents */)

            ) {
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

        __setupChildrenRequest: function (req) {
            if (this.__options["foldersOnly"]) {
                req.setParameter("foldersOnly", "true");
            }
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var tree = this._tree;
            var root = tree.getModel();
            var sel = tree.getSelection().getItem(0);
            var bt;

            var parent = this.__calcFirstFolderParent(sel);
            if (ncms.Application.userInRoles("admin.structure") || (parent !== root && parent.getAccessMask()
                .indexOf("w") != -1)) {
                bt = new qx.ui.menu.Button(this.tr("New"));
                bt.addListenerOnce("execute", this.__onNewPage, this);
                menu.add(bt);
            }

            bt = new qx.ui.menu.Button(this.tr("Refresh"));
            bt.addListenerOnce("execute", this.__onRefresh, this);
            menu.add(bt);

            if (sel != null && sel != root) {
                var canDuplicate = true;
                if (parent !== root) {
                    var am = parent.getAccessMask();
                    if (am.indexOf("w") == -1) {
                        canDuplicate = false;
                    }
                }
                if (canDuplicate) {
                    bt = new qx.ui.menu.Button(this.tr("Duplicate it"));
                    bt.addListenerOnce("execute", this.__onDuplicate, this);
                    menu.add(bt);
                }

                if ((sel.getAccessMask().indexOf("d") != -1)) {
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

                bt = new qx.ui.menu.Button(this.tr("Page references"));
                bt.addListenerOnce("execute", this.__onRefList, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Copy GUID"));
                bt.getContentElement().setAttribute("class", "copy_button");
                bt.getContentElement().setAttribute("data-clipboard-text", sel.getGuid());
                menu.add(bt);
            }
        },

        __onRefList: function (ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null || item.getGuid() == null) {
                return;
            }

            var dlg = new ncms.pgs.referrers.PageReferrersDlg(item, "Referrers pages");
            dlg.open();
        },

        __onMovePage: function (ev) {
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
                    foldersOnly: true,
                    allowRootSelection: true
                });
            dlg.addListener("completed", function (ev) {
                var target = ev.getData();
                //{"id":7,"name":"Test2","idPath":[7],"labelPath":["Test2"],"guidPath":["dd67c81082248a0241937f0ebbbd01d3"]}
                var req = new sm.io.Request(ncms.Application.ACT.getUrl("pages.move"), "PUT");
                req.setRequestContentType("application/json");
                req.setData(JSON.stringify({
                    src: item.getId(),
                    tgt: target != null ? target["id"] : 0
                }));
                req.send(function () {
                    dlg.close();
                    this._refreshNode(parent, function () {
                        if (target == null) { //refresh root
                            this._refreshNode(this._tree.getModel());
                            return;
                        }
                        this._tree.iterateOverCachedNodes(function (node) {
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

        __onChangeOrRenamePage: function (ev) {
            var tree = this._tree;
            var item = tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var parent = tree.getParent(item) || tree.getModel();
            var dlg = new ncms.pgs.PageChangeOrRenameDlg({
                id: item.getId(),
                label: item.getLabel(),
                status: item.getStatus()
            });
            dlg.addListener("completed", function (ev) {
                this._refreshNode(parent);
                dlg.close();
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(tree, false);
            }
            dlg.open();
        },

        __onDuplicate: function (ev) {
            var item = this._tree.getSelection().getItem(0);
            var parent = this._tree.getParent(item) || this._tree.getModel();
            var dlg = new ncms.pgs.PageDuplicateDlg({
                id: item.getId(),
                label: item.getLabel(),
                status: item.getStatus()
            });
            dlg.addListener("completed", function (ev) {
                this._refreshNode(parent);
                dlg.close();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __onNewPage: function (ev) {
            var parent = this.__calcFirstFolderParent(this._tree.getSelection().getItem(0));
            var parentId = parent.getId();
            var dlg = new ncms.pgs.PageNewDlg(parentId);
            dlg.addListener("completed", function (ev) {
                var item = ev.getData();
                this._refreshNode(parent, {
                    updateOnly: item.id
                });
                dlg.close();
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(this._tree, false);
            }
            dlg.open();
        },

        __onDeletePage: function (ev) {
            var item = this._tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            var parent = this._tree.getParent(item) || this._tree.getModel();
            var label = item.getLabel();
            ncms.Application.confirm(this.tr("Are you sure to remove page: %1", label), function (yes) {
                if (!yes) return;
                var url = ncms.Application.ACT.getRestUrl("pages.delete", {id: item.getId()});
                var req = new sm.io.Request(url, "DELETE", "application/json");
                req.send(function (resp) {
                    var ret = resp.getContent() || {};
                    if (ret["error"] === "ncms.page.nodel.refs.found") {
                        var dlg = new sm.alert.DefaultAlertMessages(this.tr("Unable to delete this page"));
                        dlg.addMessages("",
                            this.tr(
                                "This page cannot be removed because we found pages linked with this page. Please see the <a href=\"#\" onClick='showRefs(); return false;'>list of linked pages</a>")
                        );
                        window.showRefs = function () {
                            dlg.close();
                            delete window.showRefs;
                            var refsDlg = new ncms.pgs.referrers.PageReferrersDlg(item, "Referrers pages");
                            refsDlg.open();
                        };
                        dlg.open();
                        return;
                    }
                    this._refreshNode(parent, {
                        focus: true,
                        updateOnly: item.getId()
                    });
                }, this);
            }, this);
        },

        __onRefresh: function () {
            var item = this._tree.getSelection().getItem(0) || this._tree.getModel();
            this._refreshNode(item);
        },

        __calcFirstFolderParent: function (item) {
            var parent = item;
            while (parent && (parent.getStatus() & 1) === 0) {
                parent = this._tree.getParent(parent);
            }
            return parent || this._tree.getModel();
        }
    },

    destruct: function () {
        var events = ncms.Events.getInstance();
        events.removeListener("pageChangePublished", this.__onPagePublished, this);
        events.removeListener("pageCreated", this.__onPageCreated, this);
        events.removeListener("pageRemoved", this.__onPageEditedRemoved, this);
        events.removeListener("pageEdited", this.__onPageEditedRemoved, this);
    }
});