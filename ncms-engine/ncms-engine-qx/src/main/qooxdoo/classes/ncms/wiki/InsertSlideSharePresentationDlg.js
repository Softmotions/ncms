/**
 * Insert slideshare presentation dialog.
 */
qx.Class.define("ncms.wiki.InsertSlideSharePresentationDlg", {
    extend: qx.ui.window.Window,

    events: {
        "completed": "qx.event.type.Data"
    },

    construct: function () {
        this.base(arguments, this.tr("Insert SlideShare presentation"));
        this.setLayout(new qx.ui.layout.VBox(4));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 450
        });

        var form = this.__form = new sm.ui.form.ExtendedForm();

        var presentationIdTextField = new qx.ui.form.TextField().set({
            maxLength: 128,
            required: true
        });
        presentationIdTextField.setPlaceholder(this.tr("http://www.slideshare.com/presentation_url or embed_code"));

        form.add(presentationIdTextField, this.tr("URL or code"), this.__validateSlideShareIdentifier, "identifier",
            this, {fullRow: true});

        var customSizeCheckBox = new qx.ui.form.CheckBox();
        form.add(customSizeCheckBox, this.tr("Custom size"), null, "custom", null, {fullRow: true, flex: 1});

        var widthSpinner = new qx.ui.form.Spinner(100, 640, 3000);
        form.add(widthSpinner, this.tr("Width"), null, "width", null, {flex: 1});

        var heightSpinner = new qx.ui.form.Spinner(100, 360, 3000);
        form.add(heightSpinner, this.tr("Height"), null, "height", null, {flex: 1});

        customSizeCheckBox.bind("value", widthSpinner, "enabled");
        customSizeCheckBox.bind("value", heightSpinner, "enabled");

        var formRenderer = new sm.ui.form.ExtendedDoubleFormRenderer(form);
        formRenderer.setAllowGrowX(true);
        this.add(formRenderer);

        //Footer
        var footer = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        footer.setPadding(5);

        var okButton = this.__okButton = new qx.ui.form.Button(this.tr("Ok"));
        okButton.addListener("execute", this.__ok, this);
        footer.add(okButton);

        var cancelButton = this.__cancelButton = new qx.ui.form.Button(this.tr("Cancel"));
        cancelButton.addListener("execute", this.close, this);
        footer.add(cancelButton);

        this.add(footer);

        var escCommand = this.createCommand("Esc");
        escCommand.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members: {
        __form: null,

        __okButton: null,

        __cancelButton: null,

        __code: null,

        __isCodeLoading: false,

        __validateSlideShareIdentifier: function (identifier) {
            identifier = identifier.trim();

            if (identifier.indexOf('http') === 0) {
                this.__validateURL(identifier);
            } else {
                this.__validateCode(identifier);
            }
        },

        __validateURL: function (url) {
            var isSlideShareURL = /(http|https):\/\/(www.)?slideshare.net\/.+/.test(url);

            if (!isSlideShareURL) {
                throw new qx.core.ValidationError('Validation Error', this.tr('Invalid SlideShare URL'));
            }

            var slideShareCodeRequest = new qx.io.request.Jsonp();
            slideShareCodeRequest.setUrl('http://www.slideshare.net/api/oembed/2?url=' + url + '&format=json');
            slideShareCodeRequest.addListener("success", function (event) {
                this.__onSlideShareResponse(event.getTarget().getResponse());
            }, this);
            slideShareCodeRequest.setTimeout(5 * 1000);
            slideShareCodeRequest.addListener("fail", function (event) {
                if (this.__isCodeLoading == false) {
                    return;
                }

                if (event.getTarget().getPhase() == "statusError") {
                    this.__onInvalidResp();
                    return;
                }

                this.__onError();
            }, this);
            slideShareCodeRequest.send();

            this.__disableForm(true);
        },

        __validateCode: function (code) {
            var isNumber = /^\d*$/.test(code);

            if (!isNumber) {
                throw new qx.core.ValidationError('Validation Error', this.tr('Invalid SlideShare code'));
            }

            this.__code = code;
            this.__isCodeLoading = false;
            this.__onFormReady();
        },

        __onSlideShareResponse: function (response) {
            this.__code = response['slideshow_id'];
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

            var identifierField = this.__form.getItems()["identifier"];
            identifierField.setValid(false);
            identifierField.setInvalidMessage(this.tr('Invalid SlideShare URL'));
        },

        __onError: function () {
            this.__disableForm(false);

            var identifierField = this.__form.getItems()["identifier"];
            identifierField.setValid(false);
            identifierField.setInvalidMessage(this.tr('Failed to connect to SlideShare server. Try again later'));
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