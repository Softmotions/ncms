/**
 * Rich resource ref
 */
qx.Class.define("ncms.asm.am.RichRefAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "richref",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Rich reference");
        },

        getMetaInfo: function () {
            return ncms.asm.am.RichRefAM.__META;
        }
    },

    members: {

        _form: null,

        _imageAM: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowPages"] == null || opts["allowPages"] === "true");
            form.add(el, this.tr("Pages"), null, "allowPages", null, {flex: 0});

            el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowFiles"] === "true");
            form.add(el, this.tr("Files"), null, "allowFiles", null, {flex: 1});

            el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowName"] === "true");
            form.add(el, this.tr("Name"), null, "allowName", null, {flex: 0});

            el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowDescription"] === "true");
            form.add(el, this.tr("Extra"), null, "allowDescription", null, {flex: 1});

            el = new qx.ui.form.CheckBox();
            el.setValue(opts["optionalLinks"] === "true");
            form.add(el, this.tr("Optional links"), null, "optionalLinks", {flex: 0});

            el = new qx.ui.form.CheckBox();
            el.setValue(opts["allowImage"] === "true");
            form.add(el, this.tr("Image"), null, "allowImage", null, {flex: 1});

            //Wrap nested image am
            var imageAm = this._imageAM = new ncms.asm.am.ImageAM();
            var imageAttrSpec = sm.lang.Object.shallowClone(attrSpec);
            imageAttrSpec["options"] = opts["image"];

            var iopts = new sm.ui.form.FormWidgetAdapter(imageAm.activateOptionsWidget(imageAttrSpec, asmSpec));
            form.add(iopts, this.tr("Image"), null, "image", null, {fullRow: true});
            el.bind("value", iopts, "enabled");

            el = new qx.ui.form.TextField();
            if (opts["styles"] != null) {
                el.setValue(opts["styles"]);
            }
            el.setPlaceholder(this.tr("option=value,option2=value2,..."));
            form.add(el, this.tr("Options"), null, "styles", null, {fullRow: true});

            el = new qx.ui.form.TextField();
            if (opts["styles2"] != null) {
                el.setValue(opts["styles2"]);
            }
            el.setPlaceholder(this.tr("option=value,option2=value2,..."));
            form.add(el, this.tr("Options"), null, "styles2", null, {fullRow: true});

            el = new qx.ui.form.TextField();
            if (opts["styles3"] != null) {
                el.setValue(opts["styles3"]);
            }
            el.setPlaceholder(this.tr("option=value,option2=value2,..."));
            form.add(el, this.tr("Options"), null, "styles3", null, {fullRow: true});

            var fr = new sm.ui.form.ExtendedDoubleFormRenderer(form);//sm.ui.form.FlexFormRenderer(form);
            this._form = form;
            return fr;
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var data = {};
            var items = this._form.getItems();
            data["allowPages"] = items["allowPages"].getValue();
            data["allowFiles"] = items["allowFiles"].getValue();
            data["allowName"] = items["allowName"].getValue();
            data["optionalLinks"] = items["optionalLinks"].getValue();
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
            data["styles3"] = items["styles3"].getValue();
            return data;
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new ncms.asm.am.RichRefAMValueWidget(attrSpec, asmSpec);
            this._fetchAttributeValue(attrSpec, function (val) {
                w.setModel(JSON.parse(val));
            });
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            return this._valueWidget.valueAsJSON();
        }
    },

    destruct: function () {
        this._disposeObjects("_form", "_imageAM");
    }
});
