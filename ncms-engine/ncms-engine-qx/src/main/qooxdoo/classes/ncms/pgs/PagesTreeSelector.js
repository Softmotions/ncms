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
        //todo
        this._setLayout(new qx.ui.layout.Grow());
        this._initTree(
                {"action" : "pages.layer",
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

            if (sel != null) {
                bt = new qx.ui.menu.Button(this.tr("New page"));
                bt.addListenerOnce("execute", this.__onNewPage, this);
                menu.add(bt);
            }

        },

        __onNewPage : function(ev) {
            var d = new ncms.pgs.PageNewDlg();
            d.addListenerOnce("completed", function(ev) {
                var spec = ev.getData();
                d.destroy();
            });
            d.placeToWidget(ev.getTarget(), false);
            d.show();
        }

    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});