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
    include: [ncms.cc.MCommands],

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
        var tree = this.getChildControl("tree");

        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNewAction, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onRemove, this);
        this._registerCommandFocusWidget(tree);
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
            bt = new qx.ui.menu.Button(this.tr("New probability group"));
            bt.addListener("execute", this.__onNewGroup, this);
            menu.add(bt);

            // New composite action
            bt = new qx.ui.menu.Button(this.tr("New composite action"));
            bt.addListener("execute", this.__onNewComposite, this);
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
            var tree = this.__tree = new sm.ui.tree.ExtendedVirtualTree(null, "name", "children");
            tree.setHideRoot(true);
            tree.setDelegate({
                createItem: function () {
                    return new ncms.mtt.actions.MttActionsTreeItem();
                },
                bindItem: function (controller, item, index) {
                    controller.bindDefaultProperties(item, index);
                    controller.bindProperty("extra", "extra", null, item, index);
                    controller.bindProperty("enabled", "active", null, item, index);
                    controller.bindProperty("groupId", "groupId", null, item, index);
                    controller.bindProperty("groupWeight", "groupWeight", null, item, index);
                    controller.bindPropertyReverse("groupWeight", "groupWeight", null, item, index);
                    controller.bindProperty("ptype", "ptype", null, item, index);
                }
            });
            tree.setLabelPath("label");
            tree.setIconPath("type");
            tree.setChildProperty("children");
            tree.setIconOptions({
                converter: function (value, model, source, target) {
                    if (model.getChildren != null) {
                        var fdSuffix = target.isOpen() ? "-open" : "";
                        if (value === "composite") {
                            return "ncms/icon/22/mtt/composite" + fdSuffix + ".png";
                        } else {
                            return "ncms/icon/22/places/folder" + fdSuffix + ".png";
                        }
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
                var item = tree.getSelection().getItem(0);
                qx.core.Assert.assertNotNull(item);
                var type = item.getType();
                if (type != "group" && type != "composite") {
                    this.__onEditAction(ev);
                }
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
            bt = new qx.ui.menu.Button(this.tr("New probability group"));
            bt.addListener("execute", this.__onNewGroup, this);
            menu.add(bt);

            // New composite
            bt = new qx.ui.menu.Button(this.tr("New composite action"));
            bt.addListener("execute", this.__onNewComposite, this);
            menu.add(bt);

            if (item != null) {
                bt = new qx.ui.menu.Button(this.tr("Edit"));
                bt.addListener("execute", this.__onEditAction, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListener("execute", this.__onRemove, this);
                menu.add(bt);
            }
        },

        __onNewAction: function () {
            var tree = this.__tree;
            var parent = tree.getSelection().getItem(0);
            var type = parent && parent.getType();
            if (parent && type != "group" && type != "composite") {
                parent = null;
            }
            var dlg = new ncms.mtt.actions.MttActionDlg(this.tr("New action"), {
                ruleId: this.getRuleId(),
                enabled: true,
                groupId: (parent != null) ? parent.getId() : null
            });
            dlg.addListenerOnce("completed", function (ev) {
                var data = ev.getData();
                qx.core.Assert.assertObject(data);
                var item = this.__toTreeNode(data, true, parent);
                if (parent == null) {
                    parent = tree.getModel();
                }
                parent.getChildren().push(item);
                tree.refresh();
                tree.openNode(parent);
                dlg.close();
                tree.focus();
            }, this);
            dlg.open();
        },

        __onRemove: function () {
            var tree = this.__tree;
            var item = tree.getSelection().getItem(0);
            if (item == null || item === this.__tree.getModel()) {
                return;
            }
            ncms.Application.confirm(this.tr("Are you sure to remove action: %1", item.getLabel()), function (yes) {
                if (!yes) {
                    return;
                }
                var parent = this.__tree.getParent(item) || this.__tree.getModel();
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("mtt.action.delete", {id: item.getId()}), "DELETE");
                req.send(function () {
                    parent.getChildren().remove(item);
                    tree.refresh();
                }, this);
            }, this);
        },

        __onEditAction: function (ev) {
            var tree = this.__tree;
            var item = tree.getSelection().getItem(0);
            qx.core.Assert.assertNotNull(item);
            var type = item.getType();
            if (type === "composite") {
                return;
            }
            if (type === "group") {
                return this.__onEditGroup(ev);
            }
            var dlg = new ncms.mtt.actions.MttActionDlg(this.tr("Edit %1", item.getLabel()), {
                ruleId: this.getRuleId(),
                id: item.getId(),
                groupId: item.getGroupId(),
                type: item.getType(),
                enabled: item.getEnabled(),
                description: item.getExtra(),
                spec: JSON.parse(item.getSpec())
            });
            dlg.addListenerOnce("completed", function (ev) {
                var reg = ncms.mtt.actions.MttActionsRegistry;
                var data = ev.getData();
                item.setType(data["type"]);
                item.setExtra(data["description"]);
                item.setEnabled(!!data["enabled"]);
                item.setSpec(data["spec"]);
                var ac = reg.findMttActionClassForType(data["type"]);
                if (ac) {
                    item.setLabel(ac.specForHuman(JSON.parse(data["spec"])));
                }
                tree.refresh();
                dlg.close();
                tree.focus();
            }, this);
            dlg.open();
        },

        __onEditGroup: function (ev) {
            var tree = this.__tree;
            var item = tree.getSelection().getItem(0);
            qx.core.Assert.assertNotNull(item);
            var dlg = new ncms.mtt.actions.MttActionGroupDlg(this.getRuleId(), item);
            dlg.setPosition("bottom-center");
            dlg.addListenerOnce("completed", function (ev) {
                var data = ev.getData();
                item.setLabel(data["description"]);
                dlg.close();
                tree.focus();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.show();
        },

        __onNewGroup: function (ev) {
            return this.__onNewTypedGroup("mtt.action.group.new");
        },

        __onNewComposite: function () {
            return this.__onNewTypedGroup("mtt.action.composite.new");
        },

        __onNewTypedGroup: function (action) {
            var tree = this.__tree;
            var ruleId = this.getRuleId();
            var parent = tree.getSelection().getItem(0);
            var type = parent && parent.getType();
            if (parent && type != "group" && type != "composite") {
                parent = null;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl(action, {
                    id: ruleId
                }), "PUT", "application/json");
            if (parent != null) {
                req.setParameter("groupId", parent.getId());
            }
            req.send(function (resp) {
                var data = resp.getContent();
                qx.core.Assert.assertObject(data);
                var item = this.__toTreeNode(data, true, parent);
                if (parent == null) {
                    parent = tree.getModel();
                }
                parent.getChildren().push(item);
                tree.refresh();
                tree.openNode(parent);
                tree.focus();
            }, this);
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
            this.__tree.focus();
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
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.action.up", {id: item.getId()}),
                "POST");
            req.send(function (resp) {
                var parent = this.__tree.getParent(item) || this.__tree.getModel();
                var clist = parent.getChildren();
                var ind = clist.indexOf(item);
                clist.removeAt(ind);
                clist.insertAt(ind - 1, item);
                this.__tree.getSelection().removeAll();
                this.__tree.getSelection().push(item);
                this.__tree.refresh();
                this.__syncState();
            }, this);
        },

        __onMoveDown: function () {
            var item = this.__tree.getSelection().getItem(0);
            if (!this.__canMoveDown(item)) {
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.action.down", {id: item.getId()}),
                "POST");
            req.send(function (resp) {
                var parent = this.__tree.getParent(item) || this.__tree.getModel();
                var clist = parent.getChildren();
                var ind = clist.indexOf(item);
                clist.removeAt(ind);
                clist.insertAt(ind + 1, item);
                this.__tree.getSelection().removeAll();
                this.__tree.getSelection().push(item);
                this.__tree.refresh();
                this.__syncState();
            }, this);
        },

        __applyModel: function (value, old) {
            var tree = this.__tree;
            if (old) {
                old.removeListener("changeBubble", this.__onModelChanged, this);
            }
            if (value) {
                value.addListener("changeBubble", this.__onModelChanged, this);
            }
            tree.setModel(value);
            tree.getLookupTable().forEach(function (item) {
                if (tree.isNode(item)) {
                    tree.openNode(item);
                }
            }, this);
        },

        __onModelChanged: function (ev) {
            var data = ev.getData();
            if (qx.lang.String.endsWith(data.name, ".groupWeight")) {
                var id = data.item.getId();
                var weight = data.item.getGroupWeight();
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("mtt.action.group.weight.update", {
                        id: id,
                        weight: weight
                    }), "POST").set({
                    showMessages: true
                });
                req.send();
            }
        },

        __applyRuleId: function (id, old, cb) {
            if (id == null) {
                id = this.getRuleId();
            }
            if (id == null) {
                if (typeof cb === "function") {
                    cb();
                }
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("mtt.actions.select", {id: id}),
                "GET", "application/json");
            req.send(function (resp) {
                var data = resp.getContent();
                qx.core.Assert.assertTrue(Array.isArray(data));
                this.setModel(this.__toTreeModel(data));
                if (typeof cb === "function") {
                    cb();
                }
            }, this);
        },

        __toTreeNode: function (it, asModel, parentItem) {
            var ret;
            var ptype = parentItem != null ? parentItem.getType() : null;
            if (!it) {
                ret = {
                    id: 0,
                    groupId: null,
                    groupWeight: 0,
                    type: "root",
                    label: "root",
                    extra: "",
                    spec: "{}",
                    enabled: true,
                    ptype: null,
                    children: []
                };
            }
            if (!ret) {
                var me = this;
                var reg = ncms.mtt.actions.MttActionsRegistry;
                var groupId = it["groupId"];
                var type = it["type"];
                var spec = sm.lang.String.isEmpty(it["spec"]) ? "{}" : it["spec"];
                if (type === "group" || type === "composite") {
                    ret = {
                        id: it["id"],
                        groupId: it["groupId"] || null,
                        groupWeight: it["groupWeight"] || 0,
                        type: type,
                        label: (type === "composite")
                            ? me.tr("Composite group")
                            : me.tr("Action probability group"),
                        extra: "",
                        spec: spec,
                        enabled: !!it["enabled"],
                        ptype: ptype,
                        children: []
                    }
                } else {
                    var ac = reg.findMttActionClassForType(type);
                    if (ac == null) {
                        return null;
                    }
                    ret = {
                        id: it["id"],
                        groupId: it["groupId"] || null,
                        groupWeight: it["groupWeight"] || 0,
                        type: type,
                        label: ac.specForHuman(JSON.parse(spec)) || "",
                        extra: it["description"] || null,
                        spec: spec,
                        enabled: !!it["enabled"],
                        ptype: ptype
                    }
                }
            }
            return asModel ? qx.data.marshal.Json.createModel(ret, true) : ret;
        },

        __toTreeModel: function (data) {
            var groups = {};
            var root = this.__toTreeNode();
            data.forEach(function (it) {
                var item = this.__toTreeNode(it);
                if (item == null) {
                    return;
                }
                if (it["type"] === "group" || it["type"] === "composite") {
                    groups[item.id] = qx.lang.Object.mergeWith(groups[item.id] || {}, item, false);
                }
                if (item.groupId) {
                    groups[item.groupId] = groups[item.groupId] || {children: []};
                    groups[item.groupId].children.push(item);
                } else {
                    root.children.push(item);
                }
            }, this);
            Object.keys(groups).forEach(function (k) {
                var type = groups[k].type;
                var children = groups[k].children;
                if (Array.isArray(children)) {
                    children.forEach(function (c) {
                        c.ptype = type;
                    });
                }
            });
            return qx.data.marshal.Json.createModel(root, true);
        },

        reload: function (cb) {
            this.__applyRuleId(null, null, cb);
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