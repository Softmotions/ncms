/**
 * Tree value editor widget.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/actions/application_form_edit.png)
 * @asset(ncms/icon/16/misc/globe.png)
 * @asset(ncms/icon/16/misc/box.png)
 * @asset(ncms/icon/16/misc/block.png)
 * @asset(ncms/icon/16/misc/document-text-image.png)
 * @asset(ncms/icon/16/misc/arrow-transition-270.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 * @asset(ncms/icon/22/places/folder.png)
 * @asset(ncms/icon/22/places/folder-open.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 */
qx.Class.define("ncms.asm.am.TreeAMValueWidget", {
    extend: qx.ui.container.Composite,
    implement: [qx.ui.form.IModel,
        ncms.asm.am.IValueWidget],
    include: [ncms.asm.am.MValueWidget],

    properties: {

        appearance: {
            init: "ncms-tree-am",
            refine: true
        },

        model: {
            check: "Object",
            nullable: true,
            event: "changeModel",
            apply: "__applyModel"
        },

        syncWith: {
            check: "Number",
            nullable: true,
            apply: "__applySyncWith"
        },

        "options": {
            check: "Map",
            nullable: false,
            init: {},
            event: "changeOptions",
            apply: "__applyOptions"
        }
    },

    construct: function (attrSpec, asmSpec, model, options) {
        this.__btns = {};
        this.__broadcaster = sm.event.Broadcaster.create({
            "up": false,
            "down": false,
            "sel": false
        });
        this.__attrSpec = attrSpec;
        this.__asmSpec = asmSpec;

        this.base(arguments);

        this.setLayout(new qx.ui.layout.VBox());
        this.getChildControl("toolbar");
        this.getChildControl("tree");
        if (model != null) {
            this.setModel(model);
        }
        if (options != null) {
            this.setOptions(options);
        }
    },

    members: {

        __btns: null,

        __asmSpec: null,

        __attrSpec: null,

        __tree: null,

        __addBt: null,

        __broadcaster: null,


        _createChildControlImpl: function (id) {
            var control;
            switch (id) {
                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar();
                    control.setAppearance("toolbar-table/toolbar");
                    this._createToolbarItems(control);
                    this.add(control);
                    break;
                case "tree":
                    control = this._createTree();
                    this.add(control, {flex: 1});
                    break;
            }
            return control || this.base(arguments, id);
        },


        _createTree: function () {
            var tree = this.__tree = new qx.ui.tree.VirtualTree(null, "name", "children");
            tree.setDelegate({

                createItem: function () {
                    return new ncms.asm.am.TreeAMItem();
                },

                configureItem: function (item) {
                    item.setOpenSymbolMode("auto");
                },

                bindItem: function (controller, item, index) {
                    controller.bindDefaultProperties(item, index);
                    controller.bindProperty("extra", "extra", null, item, index);
                }
            });
            tree.setIconPath("icon");
            tree.setIconOptions({
                converter: function (value, model, source, target) {
                    switch (value) {
                        case "link":
                            return "ncms/icon/16/misc/globe.png";
                        case "page":
                            return "ncms/icon/16/misc/document-text-image.png";
                        case "file":
                            return "ncms/icon/16/misc/box.png";
                        case "block":
                            return "ncms/icon/16/misc/block.png";
                        default:
                            if (model.getChildren != null) {
                                var fdSuffix = target.isOpen() ? "-open" : "";
                                return "ncms/icon/22/places/folder" + fdSuffix + ".png";
                            } else {
                                return "icon/22/mimetypes/office-document.png";
                            }
                            break;

                    }
                }
            });

            tree.getSelection().addListener("change", function (e) {
                this.__onSelected(e.getTarget().getItem(0));
            }, this);

            tree.setContextMenu(new qx.ui.menu.Menu());
            tree.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            tree.getPane().addListener("cellDbltap", function (ev) {
                var row = ev.getRow();
                var item = tree.getLookupTable().getItem(row);
                if (tree.isNode(item)) {
                    return;
                }
                this.__onEditNA();
            }, this);
            return tree;
        },

        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            var el = this.__addBt = new qx.ui.toolbar.MenuButton(null, "ncms/icon/16/actions/add.png");
            this.__btns["add"] = el;
            el.setAppearance("toolbar-table-menubutton");
            el.setShowArrow(true);
            part.add(el);

            el = this._createButton(null, "ncms/icon/16/actions/delete.png", "delete",
                this.__onRemove, this);
            this.__broadcaster.attach(el, "sel", "enabled");
            el.setToolTipText(this.tr("Drop element"));
            part.add(el);


            el = this._createButton(this.tr("Sync with"), "ncms/icon/16/misc/arrow-transition-270.png", "sync",
                this.__onSync, this);
            el.setToolTipText(this.tr("Synchronize attribute content with another page"));
            part.add(el);

            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
            part = new qx.ui.toolbar.Part()
            .set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            el = this._createButton(null, "ncms/icon/16/misc/arrow_up.png", "up",
                this.__onMoveUp, this);
            el.setToolTipText(this.tr("Move item up"));
            this.__broadcaster.attach(el, "up", "enabled");
            part.add(el);

            el = this._createButton(null, "ncms/icon/16/misc/arrow_down.png", "down",
                this.__onMoveDown, this);
            el.setToolTipText(this.tr("Move item down"));
            this.__broadcaster.attach(el, "down", "enabled");
            part.add(el);
        },

        _createButton: function (label, icon, name, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            this.__btns[name] = bt;
            return bt;
        },

        __onSelected: function () {
            this.__syncState();
        },

        __applyModel: function (model) {
            this.__tree.setModel(model);
            /*this.__tree.getLookupTable().forEach(function(item) {
                if (this.__tree.isNode(item)) {
                    this.__tree.openNode(item);
                }
            }, this);*/
        },

        __syncState: function () {
            var item = this.__tree.getSelection().getItem(0);
            if (item == this.__tree.getModel()) {
                item = null;
            }
            var b = this.__broadcaster;
            b.setSel(item != null);
            b.setUp(this.__canMoveUp(item));
            b.setDown(this.__canMoveDown(item));
        },


        __onSync: function () {
            if (this.getSyncWith() != null) { //reset synchronization
                ncms.Application.confirm(this.tr("Are you sure to reset synchronization?"), function (yes) {
                    if (yes) {
                        this.setSyncWith(null);
                        this.fireEvent("modified");
                        this.fireEvent("requestSave");
                    }
                }, this);
                return;
            }

            var dlg = new ncms.pgs.PagesSelectorDlg(this.tr(
                "Please select the page this attribute should be synchronized"));
            dlg.addListener("completed", function (ev) {
                var pspec = ev.getData();
                var rdata = {
                    src: pspec["id"],
                    tgt: this.__asmSpec["id"],
                    attr: this.__attrSpec["name"]
                };
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("am.tree.sync"), "PUT", "application/json");
                req.setRequestContentType("application/json");
                req.setData(JSON.stringify(rdata));
                req.send(function (resp) {
                    var spec = resp.getContent();
                    if (spec != null && typeof spec["src"] === "number") {
                        this.setSyncWith(spec["src"]);
                        if (spec["tree"] != null && typeof spec["tree"] === "object") {
                            var model = qx.data.marshal.Json.createModel(spec["tree"], true);
                            this.setModel(model);
                        }
                        this.fireEvent("modified");
                        this.fireEvent("requestSave");
                    }
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        },

        __canMoveUp: function (item) {
            if (item == null) {
                return false;
            }
            var parent = this.__tree.getParent(item);
            if (parent == null) {
                return false;
            }
            return parent.getChildren().indexOf(item) > 0;
        },

        __onMoveUp: function () {
            var item = this.__tree.getSelection().getItem(0);
            if (!this.__canMoveUp(item)) {
                return;
            }
            var parent = this.__tree.getParent(item);
            var clist = parent.getChildren();
            var ind = clist.indexOf(item);
            clist.removeAt(ind);
            clist.insertAt(ind - 1, item);
            this.__tree.getSelection().removeAll();
            this.__tree.getSelection().push(item);
            this.__syncState();
            this.fireEvent("modified");
        },

        __canMoveDown: function (item) {
            if (item == null) {
                return false;
            }
            var parent = this.__tree.getParent(item);
            if (parent == null) {
                return false;
            }
            var clist = parent.getChildren();
            var ind = clist.indexOf(item);
            return ind >= 0 && ind < clist.length - 1;
        },


        __onMoveDown: function () {
            var item = this.__tree.getSelection().getItem(0);
            if (!this.__canMoveDown(item)) {
                return;
            }
            var parent = this.__tree.getParent(item);
            var clist = parent.getChildren();
            var ind = clist.indexOf(item);
            clist.removeAt(ind);
            clist.insertAt(ind + 1, item);
            this.__tree.getSelection().removeAll();
            this.__tree.getSelection().push(item);
            this.__syncState();
            this.fireEvent("modified");
        },


        __applyOptions: function (opts) {
            var bt;
            var menu = new qx.ui.menu.Menu();

            if (opts["syncWith"] != null && !isNaN(Number(opts["syncWith"]))) {
                this.setSyncWith(Number(opts["syncWith"]));
            }
            bt = new qx.ui.menu.Button(this.tr("New folder"));
            bt.addListener("execute", this.__onNewFolder, this);
            bt.addListener("appear", function (ev) {
                var bt = ev.getTarget();
                bt.setEnabled(this.__canNewFolder());
            }, this);
            menu.add(bt);


            if (opts["allowPages"] === "true") {
                bt = new qx.ui.menu.Button(this.tr("Add page reference"));
                bt.addListener("execute", this.__onAddPage, this);
                menu.add(bt);
            }
            if (opts["allowFiles"] === "true") {
                bt = new qx.ui.menu.Button(this.tr("Add file reference"));
                bt.addListener("execute", this.__onAddFile, this);
                menu.add(bt);
            }

            ncms.asm.am.TreeAM.NESTED_AMS.forEach(function (naClass) {
                var naOptions = opts[naClass.classname];
                if (naOptions == null) {
                    return;
                }
                bt = new qx.ui.menu.Button(naClass.getDescription());
                bt.setUserData("naClass", naClass);
                bt.setUserData("naOptions", naOptions);
                bt.addListener("execute", this.__onAddNA, this);
                menu.add(bt);
            }, this);

            this.__addBt.setMenu(menu);
        },

        __applySyncWith: function (val) {
            var btns = [];
            Object.keys(this.__btns).forEach(function (k) {
                if (k !== "sync") {
                    btns.push(this.__btns[k]);
                }
            }, this);
            btns.forEach(function (bt) {
                bt.setEnabled(val == null);
            });
            this.__tree.setEnabled(val == null);
            var sbt = this.__btns["sync"];
            if (val == null) {
                sbt.setLabel(this.tr("Sync with"));
            } else {
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.path",
                    {id: val}), "GET", "application/json");
                req.send(function (resp) {
                    var info = resp.getContent();
                    sbt.setLabel(this.tr("Synchronized with %1", info["labelPath"].join("/")));
                }, this);
            }
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var item = this.__tree.getSelection().getItem(0);
            if (item === this.__tree.getModel()) {
                item = null;
            }
            var opts = this.getOptions();
            var bt;

            if (this.__canNewFolder()) {
                bt = new qx.ui.menu.Button(this.tr("New folder"));
                bt.addListener("execute", this.__onNewFolder, this);
                menu.add(bt);
            }

            if (opts["allowPages"] === "true") {
                bt = new qx.ui.menu.Button(this.tr("Add page reference"));
                bt.addListener("execute", this.__onAddPage, this);
                menu.add(bt);
            }
            if (opts["allowFiles"] === "true") {
                bt = new qx.ui.menu.Button(this.tr("Add file reference"));
                bt.addListener("execute", this.__onAddFile, this);
                menu.add(bt);
            }

            this.__addBt.getMenu().getChildren().forEach(function (el) {
                var naClass = el.getUserData("naClass");
                var naOptions = el.getUserData("naOptions");
                if (naClass == null) {
                    return;
                }
                bt = new qx.ui.menu.Button(el.getLabel());
                bt.setUserData("naClass", naClass);
                bt.setUserData("naOptions", naOptions);
                bt.addListener("execute", this.__onAddNA, this);
                menu.add(bt);
            }, this);

            if (item != null) {

                menu.add(new qx.ui.menu.Separator());
                if (item.getNam && !sm.lang.String.isEmpty(item.getNam())) {
                    bt = new qx.ui.menu.Button(this.tr("Edit"));
                    bt.addListener("execute", this.__onEditNA, this);
                    menu.add(bt);
                }

                bt = new qx.ui.menu.Button(this.tr("Rename"));
                bt.addListener("execute", this.__onRename, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListener("execute", this.__onRemove, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Move to another folder"));
                bt.addListener("execute", this.__onMove, this);
                menu.add(bt);
            }
        },


        __onRename: function (ev) {
            var tree = this.__tree;
            var item = tree.getSelection().getItem(0);
            if (item == null || item === this.__tree.getModel()) {
                return;
            }
            var dlg = new sm.ui.form.SimplePromptPopupDlg(
                {
                    label: this.tr("Name"),
                    initialValue: item.getName(),
                    selectAll: true
                }
            );
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.addListenerOnce("completed", function (ev) {
                item.setName(ev.getData());
                dlg.close();
                this.fireEvent("modified");
            }, this);
            dlg.open();
        },

        __onNewFolder: function (ev) {
            if (!this.__canNewFolder()) {
                return;
            }
            var tree = this.__tree;
            var item = this.__getInsertParent();
            var dlg = new ncms.asm.am.TreeAMNewFolderDlg();
            dlg.addListener("completed", function (ev) {
                var name = ev.getData();
                dlg.close();
                var folder = qx.data.marshal.Json.createModel({
                    id: null,
                    name: name,
                    type: "folder",
                    extra: null,
                    icon: "folder",
                    link: null,
                    nam: null,
                    children: []
                }, true);
                item.getChildren().push(folder);
                tree.openNode(item);
                this.fireEvent("modified");
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __canNewFolder: function () {
            var opts = this.getOptions();
            var nl = parseInt(opts["nestingLevel"]);
            if (isNaN(nl) || nl == 0) {
                return true;
            }
            var item = this.__getInsertParent();
            var inl = this.__tree.getLevel(this.__tree.getLookupTable().indexOf(item));
            if (inl == null) {
                return false;
            }
            return (nl > inl + 1);
        },

        __onAddPage: function () {
            var opts = this.getOptions();
            var dopts = {
                includeLinkName: true,
                requireLinkName: true,
                allowExternalLinks: (opts["allowExternal"] === "true")
            };
            var tree = this.__tree;
            var item = this.__getInsertParent();
            var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Select page"), dopts);
            dlg.open();
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                // DATA:
                // {"id":18,
                // "name":"ddd",
                // "idPath":[17,18],
                // "labelPath":["p1","ddd"],
                // "guidPath":["e1174a25f4c9e0925fc3975b7643b0cc","43017c58f2af4495dd146c1d4956b38b"],
                // "linkText":"ddd",
                // "externalLink":null}
                var node = {
                    "id": data["id"] || null
                };
                if (data["externalLink"] != null) {
                    node["name"] = data["linkText"];
                    node["type"] = "link";
                    node["extra"] = data["externalLink"];
                    node["link"] = data["externalLink"];
                    node["icon"] = "link";
                    node["nam"] = null;
                } else {
                    node["name"] = data["linkText"];
                    node["type"] = "page";
                    node["extra"] = data["labelPath"].join("/");
                    node["link"] = "page:/" + sm.lang.Array.lastElement(data["guidPath"]);
                    node["icon"] = "page";
                    node["nam"] = null;
                }
                item.getChildren().push(qx.data.marshal.Json.createModel(node, true));
                tree.openNode(item);
                dlg.close();
                this.fireEvent("modified");
            }, this);
        },

        __onAddFile: function () {
            var tree = this.__tree;
            var item = this.__getInsertParent();
            var dlg = new ncms.mmgr.PageFilesSelectorDlg(
                this.__asmSpec["id"],
                this.tr("Select file for page: %1", this.__asmSpec["name"]),
                {
                    allowModify: true,
                    allowMove: false,
                    smode: qx.ui.table.selection.Model.SINGLE_SELECTION
                }
            );
            dlg.addListener("completed", function (ev) {
                //{"id":55,
                // "name":"my nsu главная (1).png",
                // "folder":"/pages/c5aa/416c/5223/e6cd/3975/5938/469b/2674/",
                // "content_type":"image/png","owner":"admin",
                // "owner_fullName":"Антон Адаманский",
                // "content_length":943086,"description":null,
                // "tags":null,"linkText":"my nsu главная (1)"}
                var data = ev.getData();
                var node = {
                    "id": data["id"] || null,
                    "name": data["linkText"],
                    "type": "file",
                    "extra": data["name"],
                    "link": null,
                    "icon": "file",
                    "nam": null
                };
                item.getChildren().push(qx.data.marshal.Json.createModel(node, true));
                tree.openNode(item);
                dlg.close();
                this.fireEvent("modified");
            }, this);
            dlg.open();
        },

        __onEditNA: function () {
            var tree = this.__tree;
            var item = tree.getSelection().getItem(0);
            if (!item.getNam || sm.lang.String.isEmpty(item.getNam())) {
                return;
            }
            var nam = item.getNam();
            nam = JSON.parse(nam);
            var naClass = qx.Class.getByName(nam["naClass"]);
            if (naClass == null) {
                return;
            }
            var naOptions = this.getOptions()[naClass.classname];
            var attrSpec = sm.lang.Object.shallowClone(this.__attrSpec);
            attrSpec["options"] = naOptions;
            attrSpec["hasLargeValue"] = false;
            attrSpec["value"] = JSON.stringify(nam);
            var dlg = new ncms.asm.am.AMWrapperDlg(naClass, attrSpec, this.__asmSpec, {
                "mode": "value"
            });
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                data["naClass"] = naClass.classname;
                var name = data["name"] || JSON.stringify(data);
                item.setName(name);
                item.setNam(JSON.stringify(data));
                dlg.close();
                this.fireEvent("modified");
            }, this);
            dlg.open();
        },

        __onAddNA: function (ev) {
            var bt = ev.getTarget();
            var tree = this.__tree;
            var item = this.__getInsertParent();
            var naClass = bt.getUserData("naClass");
            var naOptions = bt.getUserData("naOptions");
            if (naClass == null) {
                return;
            }
            var attrSpec = sm.lang.Object.shallowClone(this.__attrSpec);
            attrSpec["options"] = naOptions;
            attrSpec["hasLargeValue"] = false;
            attrSpec["value"] = "null";
            var dlg = new ncms.asm.am.AMWrapperDlg(naClass, attrSpec, this.__asmSpec, {
                "mode": "value"
            });
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                data["naClass"] = naClass.classname;
                var name = data["name"] || JSON.stringify(data);
                var node = {
                    "id": null,
                    "name": name,
                    "type": "file",
                    "extra": null,
                    "link": null,
                    "icon": "block",
                    "nam": JSON.stringify(data)
                };
                item.getChildren().push(qx.data.marshal.Json.createModel(node, true));
                tree.openNode(item);
                dlg.close();
                this.fireEvent("modified");
            }, this);
            dlg.open();
        },

        __onRemove: function () {
            var tree = this.__tree;
            var item = tree.getSelection().getItem(0);
            if (item == null || item === this.__tree.getModel()) {
                return;
            }
            var parent = tree.getParent(item);
            if (parent == null) {
                return;
            }
            parent.getChildren().remove(item);
            this.fireEvent("modified");
        },


        __onMove: function () {
            var sitem = this.__tree.getSelection().getItem(0);
            if (sitem == null) {
                return;
            }
            var model = this.__tree.getModel();
            var item2folderTree = function (item) {
                if (item === sitem) {
                    return null;
                }
                if (item !== model && item.getType() !== "folder") {
                    return null;
                }
                var obj = {
                    id: item.getId(),
                    name: item.getName(),
                    type: item.getType(),
                    extra: item.getExtra(),
                    icon: item.getIcon(),
                    link: (item.getLink ? item.getLink() : null),
                    nam: (item.getNam ? item.getNam() : null),
                    owner: item

                };
                if (item.getChildren != null) {
                    var children = obj["children"] = [];
                    item.getChildren().forEach(function (c) {
                        var ci = item2folderTree(c);
                        if (ci != null) {
                            children.push(ci);
                        }
                    });
                }
                return obj;
            };
            var ftree = item2folderTree(model);
            ftree = qx.data.marshal.Json.createModel(ftree, true);
            var dlg = new ncms.asm.am.TreeAMFoldersDlg(ftree, this.tr("Please choose the target folder"));
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                var owner = data.getOwner();
                var parent = this.__tree.getParent(sitem);
                if (parent == null) {
                    return;
                }
                var clist = parent.getChildren();
                var ind = clist.indexOf(sitem);
                if (ind === -1) {
                    return;
                }
                clist.removeAt(ind);
                owner.getChildren().push(sitem);
                this.__tree.openNode(owner);
                dlg.close();
                this.fireEvent("modified");
            }, this);
            dlg.open();
        },

        __getInsertParent: function () {
            var tree = this.__tree;
            var item = tree.getSelection().getItem(0);
            if (item && item.getChildren == null) {
                item = tree.getParent(item);
            }
            if (item == null) {
                item = tree.getModel();
            }
            return item;
        }
    },

    destruct: function () {
        this.__asmSpec = null;
        this.__attrSpec = null;
        if (this.__broadcaster) {
            this.__broadcaster.destruct();
            this.__broadcaster = null;
        }
        if (this.__tree) {
            this.__tree.getSelection().dispose();
        }
        this.__tree = null;
        this.__addBt = null;
        this.__btns = null;
        //this._disposeObjects("__field_name");
    }
});
