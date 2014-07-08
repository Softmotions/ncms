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
            var req;
            var tree = this._tree;
            var root = tree.getModel();
            var sel = tree.getSelection().getItem(0);

            var newBt = new qx.ui.menu.Button(this.tr("New page"));
            newBt.addListenerOnce("execute", this.__onNewPage, this);
            newBt.setEnabled(false);
            menu.add(newBt);

            var parent = this.__calcFirstFolderParent(sel);
            req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.check.rights", {pid : parent.getId(), rights: "w"}), "GET", "application/json");
            req.send(function(resp){
                newBt.setEnabled(!!resp.getContent());
            }, this);


            if (sel != null && sel != root) {
                var delBt = new qx.ui.menu.Button(this.tr("Drop page"));
                delBt.addListenerOnce("execute", this.__onDeletePage, this);
                delBt.setEnabled(false);
                menu.add(delBt);

                req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.check.rights", {pid : sel.getId(), rights: "d"}), "GET", "application/json");
                req.send(function(resp){
                    delBt.setEnabled(!!resp.getContent());
                }, this);
            }
        },

        __onNewPage : function(ev) {
            var parent = this.__calcFirstFolderParent(this._tree.getSelection().getItem(0));
            var parentId = parent.getId();
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
        },

        __calcFirstFolderParent : function(item) {
            var parent = item;
            while (parent && (parent.getStatus() & 1) === 0) {
                parent = this._tree.getParent(parent);
            }
            if (parent == null) {
                parent = this._tree.getModel();
            }
            return parent;
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});