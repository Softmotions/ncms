/**
 *
 */
qx.Class.define("ncms.wiki.InsertVimeoDlg", {
    extend: qx.ui.window.Window,

    events: {
        "completed": "qx.event.type.Data"
    },

    construct: function () {
        this.base(arguments, this.tr("Insert Vimeo video"));
        this.setLayout(new qx.ui.layout.VBox(4));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 450
        });

        var form = this.__form = new sm.ui.form.ExtendedForm();

        var tf = new qx.ui.form.TextField().set({
            maxLength: 128,
            required: true
        });
        tf.setPlaceholder(this.tr("https://vimeo.com/video_url or video code"));
        form.add(tf, this.tr("URL or code"),
            this.__validateVimeoId, "id", this, {fullRow: true});

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

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this.__okButton = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = this.__cancelButton = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);

        this.addListenerOnce("resize", this.center, this);
    },

    members: {
        __form: null,

        __okButton: null,

        __cancelButton: null,

        __code: null,

        __isCodeLoading: false,

        __validateVimeoId: function (id) {
            id = (id || "").trim();
            if (id.indexOf('http') === 0) {
                this.__validateURL(id);
            } else {
                this.__validateCode(id);
            }
        },

        __validateURL: function (url) {
            var isValid = /(http|https):\/\/(www.)?vimeo.com\/.+/.test(url);

            if (!isValid) {
                throw new qx.core.ValidationError('Validation Error', this.tr('Invalid Vimeo URL'));
            }

            var vimeoCodeReq = new qx.io.request.Jsonp();
            vimeoCodeReq.setUrl('https://vimeo.com/api/oembed.json?url=' + url);
            vimeoCodeReq.addListener("success", function (event) {
                this.__onVimeoResponse(event.getTarget().getResponse());
            }, this);
            vimeoCodeReq.setTimeout(5 * 1000);
            vimeoCodeReq.addListener("fail", function (event) {
                if (this.__isCodeLoading == false) {
                    return;
                }

                if (event.getTarget().getPhase() == "loading") {
                    this.__onInvalidResp();
                    return;
                }

                this.__onError();
            }, this);
            vimeoCodeReq.send();

            this.__disableForm(true);
        },

        __validateCode: function (code) {
            var isValid = /^\d+$/.test(code);

            if (!isValid) {
                throw new qx.core.ValidationError('Validation Error', this.tr('Invalid Vimeo code'));
            }

            this.__code = code;
            this.__isCodeLoading = false;
            this.__onFormReady();
        },

        __onVimeoResponse: function (response) {
            this.__code = response['video_id'];
            this.__disableForm(false);
            this.__onFormReady();
        },

        __ok: function () {
            this.__form.validate();
        },

        __disableForm: function (enabled) {
            this.__isCodeLoading = enabled;

            var fitems = this.__form.getItems();
            for (var key in fitems) {
                var item = fitems[key];
                item.setEnabled(!enabled);
            }

            this.__okButton.setEnabled(!enabled);
            this.__cancelButton.setEnabled(!enabled);
        },

        __onFormReady: function () {
            var data = {
                code: this.__code
            };

            this.__form.populateJSONObject(data);

            this.fireDataEvent("completed", data);
        },

        __onInvalidResp: function () {
            this.__disableForm(false);

            var idField = this.__form.getItems()["id"];
            idField.setValid(false);
            idField.setInvalidMessage(this.tr('Invalid Vimeo URL'));
        },

        __onError: function () {
            this.__disableForm(false);

            var idField = this.__form.getItems()["id"];
            idField.setValid(false);
            idField.setInvalidMessage(this.tr('Failed to connect to Vimeo server. Try again later'));
        },

        close: function () {
            if (this.__isCodeLoading) {
                return;
            }

            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this._disposeObjects("__form");
    }
});
