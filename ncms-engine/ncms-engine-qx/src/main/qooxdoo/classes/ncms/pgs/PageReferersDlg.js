qx.Class.define("ncms.pgs.PageReferersDlg", {
        extend: qx.ui.window.Window,

        construct: function (item, caption) {
            this.base(arguments, caption != null ? caption : this.tr("Page referers"));
            this.setLayout(new qx.ui.layout.VBox(5));
            this.set({
                modal: true,
                showMinimize: false,
                showMaximize: true,
                allowMaximize: true,
                width: 620,
                height: 400
            });

            var table = this.__table = new ncms.pgs.PageReferersInfo(item);
            this.add(table, {flex: 1});

            var cmd = this.createCommand("Esc");
            cmd.addListener("execute", this.__escPressed, this);
            this.addListenerOnce("resize", this.center, this);
        },

        members: {
            __table: null,

            __escPressed: function(){
                if(this.__table.isAttributesTabShown()){
                    this.__table.hideAttributesTab();
                    return;
                }
                this.close();
            }
        },

        destruct: function () {
            this.__table = null;
        }
    }
);