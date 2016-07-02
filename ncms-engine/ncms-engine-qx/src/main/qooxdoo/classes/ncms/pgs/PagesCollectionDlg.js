/**
 * User's pages collection dialog
 */
qx.Class.define("ncms.pgs.PagesCollectionDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * Data: {
         *   id : {Number} Page ID,
         *   name : {String} Page name,
         *   accessMask : {String} Page access mask,
         *   path : {String} Page label path starting with leading slash.
         * }
         */
        "completed": "qx.event.type.Data"
    },

    construct: function (caption, options) {
        this.__options = options || {};
        this.base(arguments, caption != null ? caption : this.tr("Page collection"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });

        var table = this.__table = new ncms.pgs.PagesCollectionTable(options);
        table.addListener("cellDbltap", this.__ok, this);
        table.addListener("syncState", this.__syncState, this);
        this.add(table, {flex: 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);


        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);

        this.__syncState();
    },

    members: {

        __options: null,

        __okBt: null,

        __table: null,

        __ok: function () {
            var page = this.__table.getSelectedPage();
            if (page == null) {
                return;
            }
            this.fireDataEvent("completed", page);
        },

        __syncState: function () {
            var page = this.__table.getSelectedPage();
            this.__okBt.setEnabled(page != null);
        }
    },

    destruct: function () {
        this.__options = null;
        this.__okBt = null;
        this.__table = null;
    }
});