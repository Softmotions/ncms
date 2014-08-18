/**
 * Simple string attribute manager.
 */
qx.Class.define("ncms.asm.am.StringAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("String");
        },

        getSupportedAttributeTypes : function() {
            return [ "string" ];
        },

        isHidden : function() {
            return false;
        }
    },

    members : {

        _form : null,

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
        activateOptionsWidget : function(attrSpec, asmSpec) {

            var form = new qx.ui.form.Form();
            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            el.add(new qx.ui.form.RadioButton(this.tr("field")).set({"model" : "field"}));
            el.add(new qx.ui.form.RadioButton(this.tr("area")).set({"model" : "area"}));
            el.setModelSelection(opts["display"] ? [opts["display"]] : ["field"]);
            form.add(el, this.tr("Display as"), null, "display");

            //---------- Text value
            var ta = new qx.ui.form.TextArea();
            this._fetchAttributeValue(attrSpec, function(val) {
                ta.setValue(val);
            });
            form.add(ta, this.tr("Value"), null, "value");

            var ph = new qx.ui.form.TextField();
            if (opts["placeholder"] != null) {
                ph.setValue(opts["placeholder"]);
            }
            form.add(ph, this.tr("Placeholder"), null, "placeholder");

            var fr = new sm.ui.form.FlexFormRenderer(form);
            fr.setLastRowFlexible();
            this._form = form;
            return fr;
        },

        optionsAsJSON : function() {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var opts = {};
            var form = this._form;
            var items = form.getItems();
            //display
            var rb = items["display"].getSelection()[0];
            opts["display"] = rb.getModel();
            opts["value"] = items["value"].getValue();
            opts["placeholder"] = items["placeholder"].getValue();
            return opts;
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var display = opts["display"] || "field";
            var w = (display === "area") ? new qx.ui.form.TextArea() : new qx.ui.form.TextField();
            this._fetchAttributeValue(attrSpec, function(val) {
                w.setValue(val);
            });
            w.setRequired(!!attrSpec["required"]);
            if (opts["placeholder"] != null) {
                w.setPlaceholder(opts["placeholder"]);
            }
            this._valueWidget = w;
            return w;
        },

        valueAsJSON : function() {
            if (this._valueWidget == null) {
                return;
            }
            return {
                value : this._valueWidget.getValue()
            }
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});