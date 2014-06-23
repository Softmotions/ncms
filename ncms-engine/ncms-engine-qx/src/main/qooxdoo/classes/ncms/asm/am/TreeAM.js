/**
 * Tree/menu
 */
qx.Class.define("ncms.asm.am.TreeAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Tree/Menu");
        },

        getSupportedAttributeTypes : function() {
            return [ "tree" ];
        }
    },


    members : {

        _form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = this._form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Allow pages"), null, "allowPages");

            el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Allow files"), null, "allowFiles");

            return new sm.ui.form.FlexFormRenderer(form);
        },

        optionsAsJSON : function() {
            var res = {};
            return res;
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            return new qx.ui.core.Widget();
        },

        valueAsJSON : function() {
            var res = {};
            return res;
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});