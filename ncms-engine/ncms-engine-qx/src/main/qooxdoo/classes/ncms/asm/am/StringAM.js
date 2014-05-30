/**
 * Simple string attribute manager.
 */
qx.Class.define("ncms.asm.am.StringAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("String attribute manager");
        },

        getSupportedAttributeTypes : function() {
            return [ "string" ];
        }
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);

    },

    members : {


        _fetchAttrValue : function(attrSpec, cb) {
            if (attrSpec == null) {
                cb(null);
                return;
            }
            if (!attrSpec["hasLargeValue"]) {
                cb(attrSpec["value"] === undefined ? null : attrSpec["value"]);
                return;
            }
            //make attribute value request
            var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("asms.attribute"),
                    "GET", "application/json");
            req.send(function(resp) {
                var attr = resp.getContent();
                var eval = attr["hasLargeValue"] ? attr["largeValue"] : attr["value"];
                cb(eval === undefined ? null : eval);
            });
        },


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
        createOptionsWidget : function(attrSpec) {
            var form = new qx.ui.form.Form();

            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));

            var fieldRb = new qx.ui.form.RadioButton(this.tr("field"));
            fieldRb.setUserData("mode", "field");

            var areaRb = new qx.ui.form.RadioButton(this.tr("area"));
            areaRb.setUserData("mode", "area");

            el.add(fieldRb);
            el.add(areaRb);

            form.add(el, this.tr("Display as"), null, "display");

            if (opts["view"] === "area") {
                areaRb.setValue(true);
            }

            //---------- Text value
            var ta = new qx.ui.form.TextArea();
            this._fetchAttrValue(attrSpec, function(val) {
                ta.setValue(val);
            });
            form.add(ta, this.tr("Value"), null, "value");

            var fr = new sm.ui.form.FlexFormRenderer(form);
            fr.setLastRowFlexible();
            return fr;
        },

        optionsAsJSON : function(w, attrSpec) {
        },

        createValueEditorWidget : function(attrSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "green"});
        },

        valueAsJSON : function(w, attrSpec) {
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});