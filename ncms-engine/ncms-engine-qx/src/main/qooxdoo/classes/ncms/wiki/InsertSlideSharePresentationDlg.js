/**
 * Insert slideshare presentation dialog.
 */
qx.Class.define("ncms.wiki.InsertSlideSharePresentationDlg", {
    extend : qx.ui.window.Window,

    events : {
        "completed" : "qx.event.type.Data"
    },

    construct : function() {
        this.base(arguments, this.tr("Insert slideshare presentation"));
        this.setLayout(new qx.ui.layout.VBox(4));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 450
        });

        var form = this.__form = new sm.ui.form.ExtendedForm();

        var presentationURLTextField = new qx.ui.form.TextField().set({
            maxLength : 128,
            required : true
        });
        presentationURLTextField.setPlaceholder(this.tr("http://www.slideshare.com/presentation_url"));

        form.add(presentationURLTextField, this.tr("Code"), this.__validateCode, "code", this, {fullRow : true});

        var customSizeCheckBox = new qx.ui.form.CheckBox();
        form.add(customSizeCheckBox, this.tr("Custom size"), null, "custom", null, {fullRow : true, flex : 1});

        var widthSpinner = new qx.ui.form.Spinner(100, 640, 3000);
        form.add(widthSpinner, this.tr("Width"), null, "width", null, {flex : 1});

        var heightSpinner = new qx.ui.form.Spinner(100, 360, 3000);
        form.add(heightSpinner, this.tr("Height"), null, "height", null, {flex : 1});

        customSizeCheckBox.bind("value", widthSpinner, "enabled");
        customSizeCheckBox.bind("value", heightSpinner, "enabled");

        var formRenderer = new sm.ui.form.ExtendedDoubleFormRenderer(form);
        formRenderer.setAllowGrowX(true);
        this.add(formRenderer);

        //Footer
        var footer = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        footer.setPadding(5);

        var okButton = new qx.ui.form.Button(this.tr("Ok"));
        okButton.addListener("execute", this.__ok, this);
        footer.add(okButton);

        var cancelButton = new qx.ui.form.Button(this.tr("Cancel"));
        cancelButton.addListener("execute", this.close, this);
        footer.add(cancelButton);

        this.add(footer);

        var escCommand = this.createCommand("Esc");
        escCommand.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members : {
        __form : null,

        __validateCode : function(code) {
            var isNumber = /^\d*$/.test(code);

            if (!isNumber) {
                throw new qx.core.ValidationError("Validation Error", this.tr("Invalid SlideShare code"));
            }

            return /^\d*$/.test(code);
        },

        __ok : function() {
            if (!this.__form.validate()) {
                return;
            }

            var data = {};
            this.__form.populateJSONObject(data);

            this.fireDataEvent("completed", data);
        },

        close : function() {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct : function() {
        this._disposeObjects("__form");
    }
});