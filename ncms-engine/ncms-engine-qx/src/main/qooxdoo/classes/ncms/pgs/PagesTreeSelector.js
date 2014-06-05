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
                    "rootLabel" : this.tr("Pages")});
        if (allowModify) {
            this.setContextMenu(new qx.ui.menu.Menu());
            this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
        }
    },

    members : {

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt;
            var tree = this._tree;
            var root = tree.getModel();
            var sel = tree.getSelection().getItem(0);

            bt = new qx.ui.menu.Button(this.tr("New page"));
            bt.addListenerOnce("execute", this.__onNewPage, this);
            menu.add(bt);

            if (sel != null) {
                if (sel != root) {
                    bt = new qx.ui.menu.Button(this.tr("Drop page"));
                    bt.addListenerOnce("execute", this.__onDeletePage, this);
                    menu.add(bt);
                }
            }
        },

        __onNewPage : function(ev) {
            var parent = this._tree.getSelection().getItem(0) || this._tree.getModel();
            var parentId = parent != null ? parent.getId() : null;
            var d = new ncms.pgs.PageNewDlg(parentId);
            d.addListenerOnce("completed", function(ev) {
                this._refreshNode(parent);
                d.destroy();
            }, this);
            d.placeToWidget(ev.getTarget(), false);
            d.show();
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
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});