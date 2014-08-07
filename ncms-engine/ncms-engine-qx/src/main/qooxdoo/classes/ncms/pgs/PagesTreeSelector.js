/**
 * Pages tree selector.
 */
qx.Class.define("ncms.pgs.PagesTreeSelector", {
    extend : qx.ui.core.Widget,
    include : [ ncms.cc.tree.MFolderTree ],

    statics : {
    },

    events : {
    },

    properties : {
    },

    construct : function(allowModify) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this._initTree(
                {"action" : "pages.layer",
                    "idPathSegments" : true,
                    "rootLabel" : this.tr("Pages"),
                    "selectRootAsNull" : true});
        if (allowModify) {
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
        }
    },

    members : {

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
                bt = new qx.ui.menu.Button(this.tr("Change/Rename"));
                bt.addListenerOnce("execute", this.__onChangeOrRenamePage, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Drop"));
                bt.addListenerOnce("execute", this.__onDeletePage, this);
                menu.add(bt);
            }
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
    }
});