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
            var form = this._form = new sm.ui.form.ExtendedForm();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Allow pages"), null, "allowPages");
            if (opts["allowPages"] == null) {
                el.setValue(true);
            } else {
                el.setValue(opts["allowPages"] == "true");
            }
            el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Allow files"), null, "allowFiles");
            el.setValue(opts["allowFiles"] == "true");

            el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Allow external links"), null, "allowExternal");
            el.setValue(opts["allowExternal"] == "true");

            el = new qx.ui.form.Spinner(1, 1, 5);
            el.setAllowGrowX(false);
            el.setSingleStep(1);
            if (opts["nestingLevel"] == null) {
                el.setValue(2);
            } else {
                el.setValue(parseInt(opts["nestingLevel"]));
            }
            form.add(el, this.tr("Nesting level"), null, "nestingLevel");
            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        optionsAsJSON : function() {
            return this._form.populateJSONObject({}, false, true);
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new ncms.asm.am.TreeAMValueWidget();
            this._fetchAttributeValue(attrSpec, function(val) {
                var opts = ncms.Utils.parseOptions(attrSpec["options"]);
                var model = qx.data.marshal.Json.createModel(JSON.parse(val), true);
                w.setModel(model);
                w.setOptions(opts);
            });
            return w;
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