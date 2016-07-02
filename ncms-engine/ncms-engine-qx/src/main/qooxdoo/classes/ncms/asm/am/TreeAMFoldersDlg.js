/**
 * Select TreeAM folders.
 *
 * @asset(ncms/icon/22/places/folder.png)
 * @asset(ncms/icon/22/places/folder-open.png)
 * @asset(qx/icon/${qx.icontheme}/22/mimetypes/office-document.png)
 */
qx.Class.define("ncms.asm.am.TreeAMFoldersDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * DATA: Selected tree model item.
         */
        "completed": "qx.event.type.Event"
    },


    construct: function (model, caption) {
        this.base(arguments, caption);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });

        var tree = this.__tree = new qx.ui.tree.VirtualTree(null, "name", "children");
        tree.setIconPath("icon");
        tree.setIconOptions({
            converter: function (value, model, source, target) {
                if (model.getChildren != null) {
                    var fdSuffix = target.isOpen() ? "-open" : "";
                    return "ncms/icon/22/places/folder" + fdSuffix + ".png";
                } else {
                    return "icon/22/mimetypes/office-document.png";
                }
            }
        });
        tree.setModel(model);
        tree.getSelection().addListener("change", this.__onChangeSelection, this);

        this.add(tree, {flex: 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        //hcont.setPadding(5);

        var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.setEnabled(false);
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members: {

        __tree: null,

        __saveBt: null,

        __onChangeSelection: function (ev) {
            this.__saveBt.setEnabled(this.__tree.getSelection().getItem(0) != null);
        },

        __ok: function () {
            var item = this.__tree.getSelection().getItem(0);
            if (item == null) {
                return;
            }
            this.fireDataEvent("completed", item);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },


    destruct: function () {
        if (this.__tree) {
            this.__tree.getSelection().removeListener("change", this.__onChangeSelection, this);
            this.__tree.resetModel();
        }
        this.__tree = null;
        this.__saveBt = null;
    }
});
