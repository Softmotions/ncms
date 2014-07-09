/**
 * Image attribute manager
 *
 * @asset(ncms/icon/16/misc/image.png)
 */
qx.Class.define("ncms.asm.am.ImageAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Image");
        },

        getSupportedAttributeTypes : function() {
            return [ "image" ];
        }
    },

    members : {

        _form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var form = this._form = new sm.ui.form.ExtendedForm();

            var wTb = new qx.ui.form.TextField().set({maxLength : 3});
            if (opts["width"] != null) {
                wTb.setValue(opts["width"]);
            }
            form.add(wTb, this.tr("Width"), null, "width");
            var hTb = new qx.ui.form.TextField().set({maxLength : 3});
            if (opts["height"] != null) {
                hTb.setValue(opts["height"]);
            }
            form.add(hTb, this.tr("Height"), null, "height");

            var autoCb = new qx.ui.form.CheckBox();
            autoCb.setValue(opts["resize"] == "true");
            autoCb.setToolTipText(
                    this.tr("Perform automatic image resizing to given height and width values"));
            form.add(autoCb, this.tr("Auto resize"), null, "resize");
            autoCb.addListener("changeValue", function(ev) {
                var val = ev.getData();
                if (val === true && restrictCb.getValue()) {
                    restrictCb.setValue(false);
                }
            });

            var restrictCb = new qx.ui.form.CheckBox();
            restrictCb.setValue(opts["restrict"] == "true");
            restrictCb.setToolTipText(
                    this.tr("Restrict image size to given height and width values"));
            form.add(restrictCb, this.tr("Restrict sizes"), null, "restrict");
            restrictCb.addListener("changeValue", function(ev) {
                var val = ev.getData();
                if (val === true && autoCb.getValue()) {
                    autoCb.setValue(false);
                }
            });

            var skipSmall = new qx.ui.form.CheckBox();
            if (opts["skipSmall"]) {
                skipSmall.setValue(opts["skipSmall"] == "true");
            } else {
                skipSmall.setValue(true);
            }
            skipSmall.setToolTipText(
                    this.tr("Skip resizing/checking image with dimensions smaller or equal to given height and width values"));
            form.add(skipSmall, this.tr("Skip small"), null, "skipSmall");

            return new qx.ui.form.renderer.Single(form)
                    .set({allowGrowX : false});
        },

        optionsAsJSON : function() {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            return this._form.populateJSONObject({});
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new ncms.asm.am.ImageAMValueWidget(attrSpec, asmSpec);
            this._fetchAttributeValue(attrSpec, function(val) {
                w.setAttributeValue(val);
            });
            this._valueWidget = w;
            return w;
        },

        valueAsJSON : function() {
            return this._valueWidget.getWidgetValue() || {};
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});
