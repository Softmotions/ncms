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


        createOptionsWidget : function(attrSpec) {
            var form = new qx.ui.form.Form();

            //---------- Options
            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            el.add(new qx.ui.form.RadioButton(this.tr("field")));
            el.add(new qx.ui.form.RadioButton(this.tr("area")));
            form.add(el, this.tr("Display as"), null, "display");

            //---------- Text value
            el = new qx.ui.form.TextArea();
            form.add(el, this.tr("Value"), null, "value");

            var fr = new sm.ui.form.FlexFormRenderer(form);
            fr.setLastRowFlexible();
            return fr;
        },

        createValueEditorWidget : function(attrSpec) {
            return new qx.ui.core.Widget().set({backgroundColor : "green"});
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});