qx.Class.define("ncms.pgs.referrers.PageReferrersDlg", {
        extend: qx.ui.window.Window,

        construct: function (item) {
            this.base(arguments);
            this.setLayout(new qx.ui.layout.VBox());
            this.set({
                modal: true,
                showMinimize: false,
                showMaximize: true,
                allowMaximize: true,
                width: 720,
                height: 500,
                contentPadding: 0
            });

            var table = this.__navPanel = new ncms.pgs.referrers.PageReferrersNav(item);
            this.add(table, {flex: 1});

            var cmd = this.createCommand("Esc");
            cmd.addListener("execute", this.close, this);
            this.addListenerOnce("resize", this.center, this);
        },

        members: {
            __navPanel: null
        },

        destruct: function () {
            this.__navPanel = null;
        }
    }
);