/**
 * Mtt actions tree.
 *
 * @asset(ncms/icon/16/actions/add.png)
 * @asset(ncms/icon/16/actions/delete.png)
 * @asset(ncms/icon/16/misc/arrow_up.png)
 * @asset(ncms/icon/16/misc/arrow_down.png)
 * @asset(ncms/icon/22/mtt/*)
 */
qx.Class.define("ncms.mtt.actions.MttActionsTree", {
    extend: qx.ui.container.Composite,
    implement: [qx.ui.form.IModel],

    properties: {

        appearance: {
            init: "ncms-tree-am",
            refine: true
        },

        /**
         * Rule ID to sync with
         */
        ruleId: {
            apply: "__applyRuleId",
            nullable: true,
            check: "Number"
        },

        model: {
            nullable: true,
            event: "changeModel",
            apply: "__applyModel",
            dereference: true
        }
    },


    construct: function (title) {
        this.__title = title;
        this.__btns = [];
        this.__broadcaster = sm.event.Broadcaster.create({
            up: false,
            down: false,
            sel: false
        });
        this.base(arguments);
        this.setLayout(new qx.ui.layout.VBox());
        this.getChildControl("toolbar");
        this.getChildControl("tree");
        // var model = qx.data.marshal.Json.createModel(spec["tree"], true);
    },

    members: {

        __broadcaster: null,

        __btns: null,

        __tree: null,

        __title: null,

        // override
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
                    control = this.__createTree();
                    this.add(control, {flex: 1});
                    break;
            }
            return control || this.base(arguments, id);
        },

        _createToolbarItems: function (toolbar) {
            var me = this;

            function _createButton(label, icon, name, handler, self) {
                var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
                me.__btns[name] = bt;
                if (handler != null) {
                    bt.addListener("execute", handler, self);
                }
                return bt;
            }

            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            // Add button
            var el = new qx.ui.toolbar.MenuButton(null, "ncms/icon/16/actions/add.png");
            var menu = new qx.ui.menu.Menu();

            // New action
            var bt = new qx.ui.menu.Button(this.tr("New action"));
            bt.addListener("execute", this.__onNewAction, this);
            menu.add(bt);

            // New action group
            bt = new qx.ui.menu.Button(this.tr("New action group"));
            bt.addListener("execute", this.__onNewGroup, this);
            menu.add(bt);

            el.setAppearance("toolbar-table-menubutton");
            el.setShowArrow(true);
            el.setMenu(menu);

            this.__btns["add"] = el;
            part.add(el);

            // Delete button
            el = _createButton(null, "ncms/icon/16/actions/delete.png", "delete",
                this.__onRemove, this);
            this.__broadcaster.attach(el, "sel", "enabled");
            el.setToolTipText(this.tr("Remove"));
            part.add(el);

            if (this.__title) {
                toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
                toolbar.add(new qx.ui.basic.Label(this.__title).set({font: "bold", alignY: "middle"}));
            }

            // Right side
            toolbar.add(new qx.ui.core.Spacer(), {flex: 1});
            part = new qx.ui.toolbar.Part()
            .set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            // Move up
            el = _createButton(null, "ncms/icon/16/misc/arrow_up.png", "up",
                this.__onMoveUp, this);
            el.setToolTipText(this.tr("Move item up"));
            this.__broadcaster.attach(el, "up", "enabled");
            part.add(el);

            // Move down
            el = _createButton(null, "ncms/icon/16/misc/arrow_down.png", "down",
                this.__onMoveDown, this);
            el.setToolTipText(this.tr("Move item down"));
            this.__broadcaster.attach(el, "down", "enabled");
            part.add(el);
        },

        __createTree: function () {
            var tree = this.__tree = new qx.ui.tree.VirtualTree(null, "name", "children");
            tree.setHideRoot(true);
            tree.setDelegate({
                createItem: function () {
                    return new ncms.mtt.actions.MttActionsTreeItem();
                },
                configureItem: function (item) {
                    item.setOpenSymbolMode("auto");
                },
                bindItem: function (controller, item, index) {
                    controller.bindDefaultProperties(item, index);
                    controller.bindProperty("extra", "extra", null, item, index);
                }
            });
            tree.setLabelPath("label");
            tree.setIconPath("type");
            tree.setChildProperty("children");
            tree.setIconOptions({
                converter: function (value, model, source, target) {
                    if (model.getChildren != null) {
                        var fdSuffix = target.isOpen() ? "-open" : "";
                        return "ncms/icon/22/places/folder" + fdSuffix + ".png";
                    } else {
                        return "ncms/icon/22/mtt/" + value + ".png";
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
                this.__onEditAction();
            }, this);
            return tree;
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var item = this.__tree.getSelection().getItem(0);
            if (item === this.__tree.getModel()) {
                item = null;
            }

            // New action
            var bt = new qx.ui.menu.Button(this.tr("New action"));
            bt.addListener("execute", this.__onNewAction, this);
            menu.add(bt);

            // New action group
            bt = new qx.ui.menu.Button(this.tr("New action group"));
            bt.addListener("execute", this.__onNewGroup, this);
            menu.add(bt);

            if (item != null) {
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListener("execute", this.__onRemove, this);
                menu.add(bt);
            }
        },

        __onNewAction: function () {
            console.log('__onNewAction');
        },

        __onEditAction: function () {
            console.log('__onEditAction');
        },

        __onNewGroup: function () {
            console.log('__onNewGroup');
        },

        __onRemove: function () {
            console.log('__onRemove');
        },

        __onSelected: function (item) {
            this.__syncState();
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

        __canMoveUp: function (item) {
            if (item == null) {
                return false;
            }
            var parent = this.__tree.getParent(item) || this.__tree.getModel();
            return parent.getChildren().indexOf(item) > 0;
        },

        __canMoveDown: function (item) {
            if (item == null) {
                return false;
            }
            var parent = this.__tree.getParent(item) || this.__tree.getModel();
            var clist = parent.getChildren();
            var ind = clist.indexOf(item);
            return ind >= 0 && ind < clist.length - 1;
        },

        __onMoveUp: function () {
            var item = this.__tree.getSelection().getItem(0);
            if (!this.__canMoveUp(item)) {
                return;
            }
            var parent = this.__tree.getParent(item) || this.__tree.getModel();
            var clist = parent.getChildren();
            var ind = clist.indexOf(item);
            clist.removeAt(ind);
            clist.insertAt(ind - 1, item);
            this.__tree.getSelection().removeAll();
            this.__tree.getSelection().push(item);
            this.__tree.refresh();
            this.__syncState();
        },

        __onMoveDown: function () {
            var item = this.__tree.getSelection().getItem(0);
            if (!this.__canMoveDown(item)) {
                return;
            }
            var parent = this.__tree.getParent(item) || this.__tree.getModel();
            var clist = parent.getChildren();
            var ind = clist.indexOf(item);
            clist.removeAt(ind);
            clist.insertAt(ind + 1, item);
            this.__tree.getSelection().removeAll();
            this.__tree.getSelection().push(item);
            this.__tree.refresh();
            this.__syncState();
        },

        __applyModel: function (value, old) {
            this.__tree.setModel(value);
            this.__tree.getLookupTable().forEach(function(item) {
                if (this.__tree.isNode(item)) {
                    this.__tree.openNode(item);
                }
            }, this);
        },

        __applyRuleId: function (id) {
            if (id == null) {
                id = this.getRuleId();
            }
            if (id == null) {
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.actions.select", {id: id}),
                "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                qx.core.Assert.assertTrue(Array.isArray(data));
                this.setModel(this.__toTreeModel(data));
            }, this);
        },

        __toTreeModel: function (data) {
            var reg = ncms.mtt.actions.MttActionsRegistry;
            var groups = {};
            var root = {
                id: 0,
                groupId: null,
                type: "root",
                label: "root",
                extra: "",
                spec: {},
                enabled: true,
                children: []
            };
            data.forEach(function (it) {
                var groupId = it["groupId"];
                var type = it["type"];
                var spec = sm.lang.String.isEmpty(it["spec"]) ? {} : JSON.parse(it["spec"]);
                if (type === "group") {
                    var group = {
                        id: it["id"],
                        groupId: it["groupId"] || null,
                        type: type,
                        label: it["description"] || "",
                        extra: "",
                        spec: spec,
                        enabled: !!it["enabled"],
                        children: []
                    };
                    groups[group.id] = qx.lang.Object.mergeWith(groups[group.id] || {}, group, false);
                    root.children.push(group);
                    return;
                }
                var ac = reg.findMttActionClassForType(type);
                if (ac == null) {
                    return;
                }
                var item = {
                    id: it["id"],
                    groupId: it["groupId"] || null,
                    type: type,
                    label: ac.specForHuman(spec) || "",
                    extra: it["description"] || null,
                    spec: spec,
                    enabled: !!it["enabled"]
                };
                if (item.groupId) {
                    groups[item.groupId] = groups[item.groupId] || {children: []};
                    groups[item.groupId].children.push(item);
                } else {
                    root.children.push(item);
                }
            }, this);
            return qx.data.marshal.Json.createModel(root, true);
        },

        reload: function () {
            this.__applyRuleId(null);
        }
    },

    destruct: function () {
        if (this.__broadcaster) {
            this.__broadcaster.destruct();
            this.__broadcaster = null;
        }
        if (this.__tree) {
            this.__tree.getSelection().dispose();
        }
        this.__tree = null;
        this.__btns = null;
        this.__title = null;
    }
});