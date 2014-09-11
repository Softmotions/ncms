qx.Class.define("ncms.asm.am.MedialineAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Medialine");
        },

        getSupportedAttributeTypes : function() {
            return [ "medialine" ];
        },

        isHidden : function() {
            return false;
        }
    },

    members : {

        __form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var el = new qx.ui.form.TextField();
            el.setRequired(true);
            if (opts["width"] != null) {
                el.setValue(opts["width"]);
            } else {
                el.setValue("800");
            }
            form.add(el, this.tr("Max image width"), sm.util.Validate.canBeRangeNumber(10, 2048), "width");
            var fr = new qx.ui.form.renderer.Single(form);
            this.__form = form;
            fr.setAllowGrowX(false);
            return fr;
        },

        optionsAsJSON : function() {
            if (this.__form == null || !this.__form.validate()) {
                return null;
            }
            var items = this.__form.getItems();
            return {
                width : items["width"].getValue()
            };
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new ncms.asm.am.MedialineAMValueWidget(asmSpec, attrSpec);
            this._fetchAttributeValue(attrSpec, function(val) {
                w.setModel((typeof val === "string") ? JSON.parse(val) : null);
            }, this);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON : function() {
            var data = {};
            return data;
        }
    },

    destruct : function() {
        this._disposeObjects("__form");
    }
});