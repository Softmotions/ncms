/**
 * Web resource reference
 */
qx.Class.define("ncms.asm.am.WebRefAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "webref",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Include web resource");
        },

        getMetaInfo: function () {
            return ncms.asm.am.WebRefAM.__META;
        }
    },

    members: {

        _form: null,

        /**
         * attrSpec example:
         *
         * {
         *   "asmId" : 1,
         *   "name" : "copyright",
         *   "type" : "string",
         *   "value" : "My company (c)",
         *   "options" : "foo=bar, foo2=bar2",
         *   "hasLargeValue" : false
         * },
         */
        activateOptionsWidget: function (attrSpec, asmSpec) {

            var form = new qx.ui.form.Form();
            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.TextField();
            el.setPlaceholder(this.tr("Please specify web resource url"));
            this._fetchAttributeValue(attrSpec, function (val) {
                el.setValue(val);
            });
            form.add(el, this.tr("Location"), null, "location");

            /*var escCb = new qx.ui.form.CheckBox();
            if (opts["escape"] != null) {
                escCb.setValue("true" == opts["escape"]);
            } else {
                escCb.setValue(true);
            }
            form.add(escCb, this.tr("Escape data"), null, "escape");*/

            var aslocCb = new qx.ui.form.CheckBox();
            aslocCb.setValue("true" == opts["asLocation"]);
            /*aslocCb.addListener("changeValue", function(ev) {
                escCb.setEnabled(ev.getData() == false);
            });*/
            form.add(aslocCb, this.tr("Render only location"), null, "asLocation");
            //escCb.setEnabled(aslocCb.getValue() == false);

            var fr = new sm.ui.form.FlexFormRenderer(form);
            this._form = form;
            return fr;
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var items = this._form.getItems();
            return {
                asLocation: items["asLocation"].getValue(),
                //escape : items["escape"].getValue(),
                value: items["location"].getValue()
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var tf = new qx.ui.form.TextField();
            tf.setPlaceholder(this.tr("Please specify web resource url"));
            this._fetchAttributeValue(attrSpec, function (val) {
                tf.setValue(val);
            });
            this._valueWidget = tf;
            return tf;
        },

        valueAsJSON: function () {
            if (this._valueWidget == null) {
                return;
            }
            return {
                value: this._valueWidget.getValue()
            }
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }
});