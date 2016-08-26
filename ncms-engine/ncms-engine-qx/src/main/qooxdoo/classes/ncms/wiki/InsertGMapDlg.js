/**
 * Insert google map iframe
 */
qx.Class.define("ncms.wiki.InsertGMapDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * Data: {
         * }
         */
        "completed": "qx.event.type.Data"
    },

    construct: function () {
        this.base(arguments, this.tr("Insert google map location"));
        this.setLayout(new qx.ui.layout.VBox(4));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620
        });
        var caption = new qx.ui.basic.Label(
            this.tr(
                "In order to add google map place please follow <a href='%1' target='_blank' rel='noopener noreferrer'>this help tutorial</a>",
                ncms.Application.APP_STATE.getHelpSiteTopicUrl("wiki.gmap")
            )
        ).set({rich: true});
        this.add(caption);

        var ta = this.__ta = new qx.ui.form.TextArea();
        ta.setMinimalLineHeight(4);
        ta.setPlaceholder(this.tr("Paste here the generated google map <iframe> element"));
        this.add(ta, {flex: 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this._okBt = new qx.ui.form.Button(this.tr("Ok"));
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

        __ta: null,

        __ok: function () {
            var data = this.__ta.getValue();
            if (sm.lang.String.isEmpty(data) ||
                /<iframe.* src="http(s)?:\/\/(www\.)?(maps\.)?google\.com\/maps\/.*>.*<\/iframe>/
                .exec(data.trim()) == null) {
                this.__ta.setValid(false);
                this.__ta.setInvalidMessage(this.tr("Invalid google map location code"));
                return;
            } else {
                this.__ta.setValid(true);
            }
            this.fireDataEvent("completed", data.trim());
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this.__ta = null;
    }
});