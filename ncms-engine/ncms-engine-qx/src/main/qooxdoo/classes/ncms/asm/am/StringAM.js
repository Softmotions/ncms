/**
 * Simple string attribute manager.
 */
qx.Class.define("ncms.asm.am.StringAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Simple string");
        },

        getSupportedAttributeTypes : function() {
            return [ "string" ];
        }
    },

    members : {


        __optionsWidget : null,

        __valueWidget : null,


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
        activateOptionsWidget : function(attrSpec) {

            var form = new qx.ui.form.Form();
            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));

            var fieldRb = new qx.ui.form.RadioButton(this.tr("field"));
            fieldRb.setModel("field");

            var areaRb = new qx.ui.form.RadioButton(this.tr("area"));
            areaRb.setModel("area");

            el.add(fieldRb);
            el.add(areaRb);

            form.add(el, this.tr("Display as"), null, "display");

            if (opts["view"] === "area") {
                areaRb.setValue(true);
            }

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
            fr.setUserData("form", form);

            this.__optionsWidget = fr;
            return fr;
        },

        optionsAsJSON : function() {
            if (this.__optionsWidget == null) {
                return null;
            }
            var opts = {};
            var form = this.__optionsWidget.getUserData("form");
            if (!form.validate()) {
                return;
            }
            var items = form.getItems();
            //display
            var rb = items["display"].getSelection()[0];
            opts["display"] = rb.getModel();
            opts["value"] = items["value"].getValue();
            opts["placeholder"] = items["placeholder"].getValue();
            return opts;
        },

        activateValueEditorWidget : function(attrSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var display = opts["display"] || "field";
            var w = (display === "area") ? qx.ui.form.TextArea() : qx.ui.form.TextField();
            this._fetchAttributeValue(attrSpec, function(val) {
                w.setValue(val);
            });
            w.setRequired(!!attrSpec["required"]);
            if (opts["placeholder"] != null) {
                w.setPlaceholder(opts["placeholder"]);
            }
            return w;
        },

        valueAsJSON : function() {
            return this.__valueWidget.getValue();
        }
    },

    destruct : function() {
        this.__optionsWidget = null;
        this.__valueWidget = null;
    }
});