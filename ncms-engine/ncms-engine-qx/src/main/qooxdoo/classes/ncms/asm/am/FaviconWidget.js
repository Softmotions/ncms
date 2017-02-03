/**
 * Form field for picking favicon.ico
 */
qx.Class.define("ncms.asm.am.FaviconWidget", {
    extend: qx.ui.core.Widget,
    implement: [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],

    include: [
        qx.ui.form.MForm,
        qx.ui.core.MChildrenHandling,
        sm.event.MForwardEvent
    ],
    properties: {

        appearance: {
            init: "sm-bt-field",
            refine: true
        },

        focusable: {
            refine: true,
            init: true
        }
    },

    events: {

        /** Fired when the value was modified */
        "changeValue": "qx.event.type.Data",

        /** Button pressed */
        "execute": "qx.event.type.Data",

        /** Reset field event */
        "reset": "qx.event.type.Event"
    },

    /**
     * @param label {String} Button label
     * @param icon {String} Button icon path
     * @param base64 {String?} widget value, Base64 encoded image
     */
    construct: function (label, icon, base64) {
        this.__label = label;
        this.__icon = icon;
        this.base(arguments);
        this._setLayout(new qx.ui.layout.HBox().set({alignY: "middle"}));
        this.__ensureControls();
        this.setValue(base64);
    },

    members: {

        __notSetPlaceholder: "not set",

        __base64: null,

        __label: null,

        __icon: null,

        __preview: null,

        setValue: function (value) {
            if (sm.lang.String.isEmpty(value)) {
                this.resetValue();
                return;
            }
            this.__base64 = value;
            this.__preview.setValue(this.__wrapBase64(value));
        },

        getValue: function () {
            return this.__base64 || "";
        },

        resetValue: function () {
            this.__base64 = null;
            this.__preview.setValue(this.tr("Not set"));
        },

        _createChildControlImpl: function (id) {
            var control;
            switch (id) {
                case "preview":
                    if (!!sm.lang.String.isEmpty(this.__base64)) {
                        control = new qx.ui.basic.Label();
                        control.set({
                            value: this.tr("Not set"),
                            rich: true,
                            marginRight: 5
                        })
                    } else {
                        control = new qx.ui.basic.Label();
                        control.set({
                            value: this.__wrapBase64(this.__base64),
                            rich: true,
                            marginRight: 5
                        });
                    }
                    control.addListener("changeValue", this.forwardEvent, this);
                    this.__preview = control;
                    this._add(control);
                    break;
                case "reset":
                    control = new qx.ui.form.Button();
                    control.addListener("execute", function () {
                        this.resetValue();
                        this.fireEvent("reset");
                    }, this);
                    control.setEnabled(false);
                    control.addState("left");
                    this._add(control);
                    break;
                case "button":
                    control = new qx.ui.form.Button(this.__label, this.__icon);
                    control.addListener("execute", function () {
                        this.__selectFile();
                    }, this);
                    control.addState("right");
                    this._add(control);
                    break;
            }

            return control || this.base(arguments, id);
        },

        __selectFile: function () {
            var me = this;
            var form = document.getElementById("ncms-upload-form");
            form.reset();
            var input = document.getElementById("ncms-upload-file");
            input.setAttribute("accept", "image/vnd.microsoft.icon");

            input.onchange = function () {
                var ico = input.files[0];
                if (ico) {
                    var reader = new FileReader();
                    reader.onload = function (event) {
                        var wrappedBase64 = event.target.result;
                        var base64 = wrappedBase64.substring(wrappedBase64.indexOf('base64,') + 7);
                        me.setValue(base64);
                    };
                    reader.readAsDataURL(ico);
                }
            };
            input.click();
        },

        __ensureControls: function () {
            var names = ["preview", "reset", "button"];
            names.forEach(function (n) {
                this.getChildControl(n);
            }, this);
            var me = this;
            this.__preview.bind("value", this.getChildControl("reset"), "enabled", {
                converter: function (v) {
                    return me.getEnabled() && !sm.lang.String.isEmpty(v) && me.__base64 != null;
                }
            })
        },

        __wrapBase64: function (base64) {
            return "<img style='display:block; height:22px; width:22px;' src='data:image/vnd.microsoft.icon;base64," + base64 + "'/>";
        },

        __destruct: function () {
            this.__base64 = null;
            this.__label = null;
            this.__icon = null;
            this.__preview = null;
        }
    },

    destruct: function () {
        this.__destruct();
    }
});

