/**
 * Image attribute manager
 *
 * @asset(ncms/icon/16/misc/image.png)
 */
qx.Class.define("ncms.asm.am.ImageAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META:  {
            attributeTypes: "image",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Image");
        },

        getMetaInfo: function () {
            return ncms.asm.am.ImageAM.__META;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var form = this._form = new sm.ui.form.ExtendedForm();

            var wTb = new qx.ui.form.TextField().set({maxLength: 3});
            if (opts["width"] != null) {
                wTb.setValue(opts["width"]);
            }
            form.add(wTb, this.tr("Width"), sm.util.Validate.canBeRangeNumber(10, 2048, true), "width");

            var hTb = new qx.ui.form.TextField().set({maxLength: 3});
            if (opts["height"] != null) {
                hTb.setValue(opts["height"]);
            }
            form.add(hTb, this.tr("Height"), sm.util.Validate.canBeRangeNumber(10, 2048, true), "height");

            wTb.addListener("input", function () {
                coverCb.setEnabled(!sm.lang.String.isEmpty(wTb.getValue()) && !sm.lang.String.isEmpty(hTb.getValue()))
            });

            hTb.addListener("input", function () {
                coverCb.setEnabled(!sm.lang.String.isEmpty(wTb.getValue()) && !sm.lang.String.isEmpty(hTb.getValue()))
            });

            var autoCb = new qx.ui.form.CheckBox();
            autoCb.setValue(opts["resize"] === "true");
            autoCb.setToolTipText(
                this.tr("Automatic image resizing to given height and width values"));
            form.add(autoCb, this.tr("Auto resize"), null, "resize");
            autoCb.addListener("changeValue", function (ev) {
                var val = ev.getData();
                if (val === true) {
                    restrictCb.setValue(false);
                    coverCb.setValue(false);
                }
            });

            var coverCb = new qx.ui.form.CheckBox();
            coverCb.setValue(opts["cover"] === "true");
            coverCb.setToolTipText(
                this.tr("Automatic image resizing to cover given area. Image's original aspect ratio is preserved.")
            );
            coverCb.setEnabled(!sm.lang.String.isEmpty(wTb.getValue()) && !sm.lang.String.isEmpty(hTb.getValue()));
            coverCb.addListener("changeValue", function (ev) {
                var val = ev.getData();
                if (val === true) {
                    autoCb.setValue(false);
                    restrictCb.setValue(false);
                    skipSmall.setValue(false);
                }
            });
            form.add(coverCb, this.tr("Cover area"), null, "cover");

            var restrictCb = new qx.ui.form.CheckBox();
            restrictCb.setValue(opts["restrict"] === "true");
            restrictCb.setToolTipText(
                this.tr("Restrict image size to given height and width values"));
            form.add(restrictCb, this.tr("Restrict size"), null, "restrict");
            restrictCb.addListener("changeValue", function (ev) {
                var val = ev.getData();
                if (val === true) {
                    autoCb.setValue(false);
                    coverCb.setValue(false);
                }
            });

            var skipSmall = new qx.ui.form.CheckBox();
            if (opts["skipSmall"]) {
                skipSmall.setValue(opts["skipSmall"] === "true");
            } else {
                skipSmall.setValue(true);
            }
            skipSmall.setToolTipText(
                this.tr(
                    "Skip resizing/checking image with dimensions smaller or equal to given height and width values"));
            skipSmall.addListener("changeValue", function (ev) {
                var val = ev.getData();
                if (val === true) {
                    coverCb.setValue(false);
                }
            });
            form.add(skipSmall, this.tr("Skip small"), null, "skipSmall");

            return new qx.ui.form.renderer.Single(form)
            .set({allowGrowX: false});
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            return this._form.populateJSONObject({});
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new ncms.asm.am.ImageAMValueWidget(attrSpec, asmSpec);
            this._fetchAttributeValue(attrSpec, function (val) {
                w.setModel(val);
            });
            w.setRequired(!!attrSpec["required"]);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            return this._valueWidget.getWidgetValue() || {};
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }
});
