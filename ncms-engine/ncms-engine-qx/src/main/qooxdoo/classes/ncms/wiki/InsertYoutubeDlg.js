/**
 * Insert youtube video dialog.
 */
qx.Class.define("ncms.wiki.InsertYoutubeDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * Data:
         * {"code":"1m8lFg3e8AE",       //Youtube video code
         * "custom":false,              //True if video size was overridden
         * "width":640,"height":360     //Custom video size
         * }
         */
        "completed": "qx.event.type.Data"
    },

    construct: function () {
        this.base(arguments, this.tr("Insert Youtube video"));
        this.setLayout(new qx.ui.layout.VBox(4));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 450
        });

        var form = this.__form = new sm.ui.form.ExtendedForm();


        var tf = new qx.ui.form.TextField()
        .set({maxLength: 128, required: true});
        tf.setPlaceholder(this.tr("http://www.youtube.com/watch?v=? or video code"));
        form.add(tf, this.tr("URL or code"),
            this.__validateYoutubeId, "code", this, {fullRow: true});

        var cb = new qx.ui.form.CheckBox();
        form.add(cb, this.tr("Custom size"), null, "custom", null, {fullRow: true, flex: 1});

        var wsp = new qx.ui.form.Spinner(100, 640, 3000);
        form.add(wsp, this.tr("Width"), null, "width", null, {flex: 1});

        var hsp = new qx.ui.form.Spinner(100, 360, 3000);
        form.add(hsp, this.tr("Height"), null, "height", null, {flex: 1});

        cb.bind("value", wsp, "enabled");
        cb.bind("value", hsp, "enabled");

        var fr = new sm.ui.form.ExtendedDoubleFormRenderer(form);
        fr.setAllowGrowX(true);
        this.add(fr);

        //Footer
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

        __form: null,

        __validateYoutubeId: function (val) {
            var id = this.__getYoutubeId(val);
            if (id == null) {
                throw new qx.core.ValidationError("Validation Error", this.tr("Invalid YouTube video url or code"));
            }
        },

        __getYoutubeId: function (val) {
            if (val == null) {
                return null;
            }
            val = val.trim();
            var res = /(http|https):\/\/(www.)?youtube.com\/watch\?v=((\w|\-){3,15})[\w#!:\.\+=&%@!\-\/]*/.exec(val);
            if (res == null) {
                res = /^(\w|\-){3,15}$/.exec(val);
                return res ? res[0] : null;
            }
            return res[3];
        },

        __ok: function () {
            if (!this.__form.validate()) {
                return;
            }
            var data = {};
            this.__form.populateJSONObject(data);
            if (data["code"]) {
                data["code"] = this.__getYoutubeId(data["code"]);
            }
            this.fireDataEvent("completed", data);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this._disposeObjects("__form");
    }
});