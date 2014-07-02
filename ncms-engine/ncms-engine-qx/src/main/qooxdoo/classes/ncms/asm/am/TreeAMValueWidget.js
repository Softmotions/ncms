/**
 * Tree value editor widget.
 *
 * @asset(ncms/icon/22/places/folder.png)
 * @asset(ncms/icon/22/places/folder-open.png)
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/actions/application_form_edit.png)
 * @asset(ncms/icon/16/misc/globe.png)
 * @asset(ncms/icon/16/misc/box.png)
 * @asset(ncms/icon/16/misc/document-text-image.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 */
qx.Class.define("ncms.asm.am.TreeAMValueWidget", {
    extend : qx.ui.container.Composite,
    implement : [ qx.ui.form.IModel,
                  ncms.asm.am.IValueWidget],
    include : [ ncms.asm.am.MValueWidget ],

    properties : {

        appearance : {
            init : "ncms-tree-am",
            refine : true
        },

        model : {
            check : "Object",
            nullable : true,
            event : "changeModel",
            apply : "__applyModel"
        },

        "options" : {
            check : "Map",
            nullable : false,
            init : {},
            event : "changeOptions",
            apply : "__applyOptions"
        }
    },

    construct : function(asmSpec, model, options) {
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
        this.__asmSpec = asmSpec;
    },

    members : {

        __asmSpec : null,

        __tree : null,

        __addBt : null,

        __delBt : null,

        _createChildControlImpl : function(id) {
            var control;
            switch (id) {
                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar();
                    this._createToolbarItems(control);
                    this.add(control);
                    break;
                case "tree":
                    control = this._createTree();
                    this.add(control, {flex : 1});
                    break;
            }
            return control || this.base(arguments, id);
        },


        _createTree : function() {
            var tree = this.__tree = new qx.ui.tree.VirtualTree(null, "name", "children");
            var me = this;
            var delegate = {
                /*createItem : function() {
                 return new sm.ui.tree.ExtendedVirtualTreeItem();
                 },*/

                configureItem : function(item) {
                    item.setOpenSymbolMode("auto");
                },

                bindItem : function(controller, item, index) {
                    controller.bindDefaultProperties(item, index);
                }
            };
            tree.setDelegate(delegate);
            tree.setIconPath("icon");
            tree.setIconOptions({
                converter : function(value, model, source, target) {
                    switch (value) {
                        case "link":
                            return "ncms/icon/16/misc/globe.png";
                        case "page":
                            return "ncms/icon/16/misc/document-text-image.png";
                        case "file":
                            return "ncms/icon/16/misc/box.png";
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

            tree.setContextMenu(new qx.ui.menu.Menu());
            tree.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            return tree;
        },

        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            var el = this.__addBt = new qx.ui.toolbar.MenuButton(null, "ncms/icon/16/actions/add.png");
            el.setAppearance("toolbar-table-menubutton");
            el.setShowArrow(true);
            part.add(el);

            this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png",
                    this.__onRemove, this);
            this.__delBt.setToolTipText(this.tr("Drop element"));
            part.add(this.__delBt);
        },

        _createButton : function(label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance" : "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        __applyModel : function(model) {
            this.__tree.setModel(model);
        },

        __applyOptions : function(opts) {
            var bt;
            var menu = new qx.ui.menu.Menu();

            bt = new qx.ui.menu.Button(this.tr("New folder"));
            bt.addListener("execute", this.__onNewFolder, this);
            menu.add(bt);

            if (opts["allowPages"] == "true") {
                bt = new qx.ui.menu.Button(this.tr("Add page reference"));
                bt.addListener("execute", this.__onAddPage, this);
                menu.add(bt);
            }
            if (opts["allowFiles"] == "true") {
                bt = new qx.ui.menu.Button(this.tr("Add file reference"));
                bt.addListener("execute", this.__onAddFile, this);
                menu.add(bt);
            }
            this.__addBt.setMenu(menu);
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var opts = this.getOptions();
            var bt;

            bt = new qx.ui.menu.Button(this.tr("New folder"));
            bt.addListener("execute", this.__onNewFolder, this);
            menu.add(bt);

            if (opts["allowPages"] == "true") {
                bt = new qx.ui.menu.Button(this.tr("Add page reference"));
                bt.addListener("execute", this.__onAddPage, this);
                menu.add(bt);
            }
            if (opts["allowFiles"] == "true") {
                bt = new qx.ui.menu.Button(this.tr("Add file reference"));
                bt.addListener("execute", this.__onAddFile, this);
                menu.add(bt);
            }

            bt = new qx.ui.menu.Button(this.tr("Remove"));
            bt.addListener("execute", this.__onRemove, this);
            menu.add(bt);
        },


        __onNewFolder : function(ev) {
            var tree = this.__tree;
            var item = this.__getInsertParent();
            var dlg = new ncms.asm.am.TreeAMNewFolderDlg();
            dlg.addListener("completed", function(ev) {
                var name = ev.getData();
                dlg.close();
                var folder = qx.data.marshal.Json.createModel({
                    id : null,
                    name : name,
                    type : "folder",
                    extra : null,
                    icon : "folder",
                    children : []
                }, true);
                item.getChildren().push(folder);
                tree.openNode(item);
                this.fireEvent("modified");
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __onAddPage : function() {
            var opts = this.getOptions();
            var dopts = {
                includeLinkName : true,
                requireLinkName : true,
                allowExternalLinks : (opts["allowExternal"] == "true")
            };
            var tree = this.__tree;
            var item = this.__getInsertParent();
            var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Select page"), dopts);
            dlg.open();
            dlg.addListener("completed", function(ev) {
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
                    "id" : data["id"]
                };
                if (data["externalLink"] != null) {
                    node["name"] = data["linkText"] + " (" + data["externalLink"] + ")";
                    node["type"] = "link";
                    node["extra"] = data["externalLink"];
                    node["icon"] = "link";

                } else {
                    var name = data["linkText"];
                    name += " (";
                    name += data["labelPath"].join("/");
                    name += ")";
                    node["name"] = name;
                    node["type"] = "page";
                    node["extra"] = null;
                    node["icon"] = "page";
                }
                item.getChildren().push(qx.data.marshal.Json.createModel(node, true));
                tree.openNode(item);
                dlg.close();
                this.fireEvent("modified");
            }, this);
        },

        __onAddFile : function() {
            var tree = this.__tree;
            var item = this.__getInsertParent();
            var dlg = new ncms.mmgr.PageFilesSelectorDlg(
                    this.__asmSpec["guid"],
                    this.tr("Select file for page: %1", this.__asmSpec["name"]),
                    {
                        "allowModify" : true,
                        "allowMove" : false,
                        "smode" : qx.ui.table.selection.Model.SINGLE_SELECTION
                    }
            );
            dlg.addListener("completed", function(ev) {
                //data={"id":18,"name":"nlogo.jpg","folder":"/pages/162d/ab35/f252/f014/695e/6e66/5f63/bbcb/",
                // "content_type":"image/jpeg","owner":"admin","owner_fullName":"Антон Адаманский",
                // "content_length":16519,"description":"dslsd;lds;","tags":null}
                var data = ev.getData();
                var node = {
                    "id" : data["id"],
                    "name" : data["name"],
                    "type" : "file",
                    "extra" : data["folder"] + data["name"],
                    "icon" : "file"
                };
                item.getChildren().push(qx.data.marshal.Json.createModel(node, true));
                tree.openNode(item);
                dlg.close();
                this.fireEvent("modified");
            }, this);
            dlg.open();
        },

        __onRemove : function() {
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


        __onEdit : function() {

        },


        __getInsertParent : function() {
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

    destruct : function() {
        this.__tree = null;
        this.__addBt = null;
        this.__delBt = null;
        //this._disposeObjects("__field_name");
    }
});
