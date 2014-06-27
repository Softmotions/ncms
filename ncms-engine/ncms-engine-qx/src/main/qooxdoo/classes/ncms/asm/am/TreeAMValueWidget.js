/**
 * Tree value editor widget.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/actions/application_form_edit.png)
 */
qx.Class.define("ncms.asm.am.TreeAMValueWidget", {
    extend : qx.ui.container.Composite,
    implement : [ qx.ui.form.IModel ],

    statics : {
    },

    events : {
    },

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

    construct : function(model, options) {
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

    members : {

        __tree : null,

        __addBt : null,

        __delBt : null,

        __editBt : null,

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
            tree.setHideRoot(true);
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

            this.__editBt = this._createButton(null, "ncms/icon/16/actions/application_form_edit.png",
                    this.__onEdit, this);
            part.add(this.__editBt);
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
            menu.add(bt);
        },

        __onAddPage : function() {
            var opts = this.getOptions();
            var dopts = {
                includeLinkName : true,
                requireLinkName : true,
                allowExternalLinks : (opts["allowExternal"] == "true")
            };
            var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Select page"), dopts);
            dlg.open();
            dlg.addListener("completed", function(ev) {
                var data = ev.getData();
                qx.log.Logger.info("DATA=" + JSON.stringify(data));
            }, this);
        },


        __onAddFile : function() {
            qx.log.Logger.info("add file");
        },

        __onRemove : function() {

        },

        __onEdit : function() {

        }
    },

    destruct : function() {
        this.__tree = null;
        this.__addBt = null;
        this.__delBt = null;
        this.__editBt = null;
        //this._disposeObjects("__field_name");                                
    }
});
