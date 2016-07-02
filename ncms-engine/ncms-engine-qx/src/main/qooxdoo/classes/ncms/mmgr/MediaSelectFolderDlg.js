/**
 * Select folder dialog.
 */
qx.Class.define("ncms.mmgr.MediaSelectFolderDlg", {
    extend: qx.ui.window.Window,
    include: [ncms.cc.tree.MFolderTree],

    events: {
        /**
         * DATA: {Array} Path segments to the selected file item.
         */
        "completed": "qx.event.type.Data"
    },

    properties: {},

    construct: function (caption, icon) {
        this.base(arguments, caption, icon);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });
        this._initTree({"action": "media.folders"});

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);

        this.addListener("itemSelected", function (ev) {
            var item = ev.getData();
            this.__saveBt.setEnabled(item != null);
        }, this);
    },

    members: {

        __saveBt: null,

        close: function () {
            this.base(arguments);
            this.destroy();
        },

        __init: function (tree) {
            this.add(tree, {flex: 1});
            var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
            hcont.setPadding(5);

            var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Ok")).set({enabled: false});
            bt.addListener("execute", this._ok, this);
            hcont.add(bt);

            bt = new qx.ui.form.Button(this.tr("Cancel"));
            bt.addListener("execute", this.close, this);
            hcont.add(bt);
            this.add(hcont);
        },

        _ok: function (evt) {
            this.fireDataEvent("completed", this._getItemPathSegments(this._tree.getSelection().getItem(0)));
        },

        __dispose: function () {
            this.__saveBt = null;
        }
    },

    defer: function (statics, members) {
        members.addTree = members.__init;
    },

    destruct: function () {
        this.__dispose();
    }
});