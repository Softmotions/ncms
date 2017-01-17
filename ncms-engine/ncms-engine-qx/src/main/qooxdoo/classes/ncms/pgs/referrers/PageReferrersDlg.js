qx.Class.define("ncms.pgs.referrers.PageReferrersDlg", {
        extend: qx.ui.window.Window,

        // properties: {
        //     appearance: {
        //         refine: true,
        //         init: "dlg-window/caption"
        //     }
        // },

        construct: function (item, caption) {
            this.base(arguments, caption);
            this.setLayout(new qx.ui.layout.VBox());
            this.set({
                modal: true,
                showMinimize: false,
                showMaximize: true,
                allowMaximize: true,
                width: 620,
                height: 400
            });

            var table = this.__navPanel = new ncms.pgs.referrers.PageReferrersNav(item);
            this.add(table, {flex: 1});

            var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5, "right"));
            hcont.setPadding(5);

            var okButton = new qx.ui.form.Button("Close");
            okButton.addListener("execute", this.close, this);
            hcont.add(okButton);
            this.add(hcont);

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