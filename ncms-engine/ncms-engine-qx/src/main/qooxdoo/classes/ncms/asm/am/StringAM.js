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


        __optionsWidget : null,

        __valueWidget : null,


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
        activateOptionsWidget : function(attrSpec) {

            var form = new qx.ui.form.Form();
            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));

            var fieldRb = new qx.ui.form.RadioButton(this.tr("field"));
            fieldRb.setUserData("display", "field");

            var areaRb = new qx.ui.form.RadioButton(this.tr("area"));
            areaRb.setUserData("display", "area");

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
            for (var k in items) {
                var item = items[k];
                if (k === "display") {
                    var rb = item.getSelection()[0];
                    opts["display"] = rb.getUserData("display");
                } else {
                    opts[k] = item.getValue();
                }
            }
            return opts;
        },

        activateValueEditorWidget : function(attrSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "green"});
        },

        valueAsJSON : function() {
            return {};
        },

        __disposeWidget : function(w) {
            if (w) {
                w.setUserData("form", null);
                w.destroy();
            }
        }
    },

    destruct : function() {
        this.__disposeWidget(this.__optionsWidget);
        this.__disposeWidget(this.__valueWidget);
        this.__optionsWidget = null;
        this.__valueWidget = null;
    }
});