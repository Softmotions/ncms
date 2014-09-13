/**
 * Rich resource ref
 */
qx.Class.define("ncms.asm.am.RichRefAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Rich reference");
        },

        getSupportedAttributeTypes : function() {
            return [ "richref" ];
        },

        isHidden : function() {
            return false;
        }
    },

    members : {

        _form : null,

        _imageAM : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowDescription"] === "true");
            form.add(el, this.tr("Allow description"), null, "allowDescription");

            el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowImage"] === "true");
            form.add(el, this.tr("Allow image"), null, "allowImage");

            //Wrap nested image am
            var imageAm = this._imageAM = new ncms.asm.am.ImageAM();
            var imageAttrSpec = sm.lang.Object.shallowClone(attrSpec);
            imageAttrSpec["options"] = opts["image"];

            var iopts = new sm.ui.form.FormWidgetAdapter(imageAm.activateOptionsWidget(imageAttrSpec, asmSpec));
            form.add(iopts, this.tr("Image"), null, "image");
            el.bind("value", iopts, "enabled");

            el = new qx.ui.form.TextField();
            if (opts["styles"] != null) {
                el.setValue(opts["styles"]);
            }
            el.setPlaceholder(this.tr("style=value,style2=value2,..."));
            form.add(el, this.tr("Styles"), null, "styles");

            el = new qx.ui.form.TextField();
            if (opts["styles2"] != null) {
                el.setValue(opts["styles2"]);
            }
            el.setPlaceholder(this.tr("style=value,style2=value2,..."));
            form.add(el, this.tr("Styles2"), null, "styles2");


            var fr = new sm.ui.form.FlexFormRenderer(form);
            this._form = form;
            return fr;
        },

        optionsAsJSON : function() {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var data = {};
            var items = this._form.getItems();
            data["allowDescription"] = items["allowDescription"].getValue();
            data["allowImage"] = items["allowImage"].getValue();
            if (data["allowImage"]) {
                data["image"] = this._imageAM.optionsAsJSON();
                if (data["image"] == null) {
                    return;
                }
            }
            data["styles"] = items["styles"].getValue();
            data["styles2"] = items["styles2"].getValue();
            return data;
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new ncms.asm.am.RichRefAMValueWidget(attrSpec, asmSpec);
            this._fetchAttributeValue(attrSpec, function(val) {
                w.setModel(JSON.parse(val));
            });
            this._valueWidget = w;
            return w;
        },

        valueAsJSON : function() {
            return this._valueWidget.valueAsJSON();
        }
    },

    destruct : function() {
        this._disposeObjects("_form", "_imageAM");
    }
});
