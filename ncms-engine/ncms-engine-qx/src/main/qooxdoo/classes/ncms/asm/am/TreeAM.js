/**
 * Tree/menu
 *
 * @asset(ncms/icon/16/misc/gear.png)
 */
qx.Class.define("ncms.asm.am.TreeAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "tree",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Tree/Menu");
        },

        getMetaInfo: function () {
            return ncms.asm.am.TreeAM.__META;
        },

        NESTED_AMS: [
            ncms.asm.am.RichRefAM
        ]
    },


    members: {

        _form: null,

        _opts: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = this._form = new sm.ui.form.ExtendedForm();
            var opts = this._opts = ncms.Utils.parseOptions(attrSpec["options"]);

            var el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Pages"), null, "allowPages");
            if (opts["allowPages"] == null) {
                el.setValue(true);
            } else {
                el.setValue(opts["allowPages"] == "true");
            }
            el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("Files"), null, "allowFiles");
            el.setValue(opts["allowFiles"] == "true");

            el = new qx.ui.form.CheckBox();
            form.add(el, this.tr("External links"), null, "allowExternal");
            el.setValue(opts["allowExternal"] == "true");

            el = new qx.ui.form.Spinner(1, 2, 5);
            el.setAllowGrowX(false);
            el.setSingleStep(1);
            if (opts["nestingLevel"] == null) {
                el.setValue(2);
            } else {
                el.setValue(parseInt(opts["nestingLevel"]));
            }
            form.add(el, this.tr("Nesting level"), null, "nestingLevel");

            var NESTED_AMS = ncms.asm.am.TreeAM.NESTED_AMS;
            for (var i = 0; i < NESTED_AMS.length; ++i) {
                var clazz = NESTED_AMS[i];
                el = new sm.ui.form.IconCheckBox("ncms/icon/16/misc/gear.png");
                el.setUserData("naClass", clazz);
                el.setUserData("naOptions", opts[clazz.classname]);
                el.setValue(opts[clazz.classname] != null);
                el.addListener("iconClicked", function (ev) {
                    this.__openNAMCSettings(ev, attrSpec, asmSpec);
                }, this);
                form.add(el, clazz.getDescription(), null, clazz.classname);
            }

            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        optionsAsJSON: function () {
            var opts = this._form.populateJSONObject({}, false, true);
            var items = this._form.getItems();
            for (var k in items) {
                var w = items[k];
                var naClass = w.getUserData("naClass");
                if (naClass == null) {
                    continue;
                }
                if (w.getValue() == false) { //not checked
                    delete opts[naClass.classname];
                    continue;
                }
                opts[naClass.classname] = w.getUserData("naOptions");
            }
            return opts;
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var w = new ncms.asm.am.TreeAMValueWidget(attrSpec, asmSpec);
            this._fetchAttributeValue(attrSpec, function (val) {
                var opts = ncms.Utils.parseOptions(attrSpec["options"]);
                var model = qx.data.marshal.Json.createModel(JSON.parse(val), true);
                w.setModel(model);
                w.setOptions(opts);
            });
            this._valueWidget = w;
            return w;
        },

        valueAsJSON: function () {
            var w = this._valueWidget;
            var model = w.getModel();
            if (model == null) {
                return {};
            }
            model = qx.util.Serializer.toNativeObject(model);
            model["syncWith"] = w.getSyncWith();
            return model;
        },

        __openNAMCSettings: function (ev, attrSpec, asmSpec) {
            var w = ev.getTarget();
            var clazz = w.getUserData("naClass");
            var naOpts = ncms.Utils.parseOptions(w.getUserData("naOptions"));
            attrSpec = sm.lang.Object.shallowClone(attrSpec);
            attrSpec["options"] = naOpts;
            var dlg = new ncms.asm.am.AMWrapperDlg(clazz, attrSpec, asmSpec, {
                mode: "options"
            });
            //{"allowDescription":true,"allowImage":true,"image":{"width":"10","height":null,"resize":false,"restrict":false,"skipSmall":true}}
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                w.setUserData("naOptions", data);
                dlg.close();
            }, this);
            dlg.open();
        }
    },

    destruct: function () {
        this._opts = null;
        this._disposeObjects("_form");
    }
});