/**
 * Markdown/Mediawiki attribute manager.
 */
qx.Class.define("ncms.asm.am.WikiAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Wiki editor");
        },

        getSupportedAttributeTypes : function() {
            return [ "wiki" ];
        }
    },

    members : {

        _form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = this._form = new sm.ui.form.ExtendedForm();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            el.add(new qx.ui.form.RadioButton(this.tr("mediawiki")).set({"model" : "mediawiki"}));
            el.add(new qx.ui.form.RadioButton(this.tr("markdown")).set({"model" : "markdown"}));
            el.setModelSelection(opts["markup"] ? [opts["markup"]] : ["mediawiki"]);
            form.add(el, this.tr("Markup language"), null, "markup");

            return new sm.ui.form.FlexFormRenderer(form);
        },

        optionsAsJSON : function() {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var res = this._form.populateJSONObject({}, false, true);
            var items = this._form.getItems();
            var sel = items["markup"].getSelection()[0];
            res["markup"] = (sel != null) ? sel.getModel() : "mediawiki";
            return res;
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new ncms.wiki.WikiEditor();
            w.setHelpSite("http://nsu.ru");
            return w;
        },

        valueAsJSON : function() {
            return {};
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});