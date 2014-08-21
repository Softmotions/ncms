/**
 * Tree/menu
 *
 * @asset(ncms/icon/16/misc/gear.png)
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
        },

        isHidden : function() {
            return false;
        }
    },


    members : {

        _form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = this._form = new sm.ui.form.ExtendedForm();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

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

            el = new qx.ui.form.Spinner(0, 1, 5);
            el.setAllowGrowX(false);
            el.setSingleStep(1);
            if (opts["nestingLevel"] == null) {
                el.setValue(2);
            } else {
                el.setValue(parseInt(opts["nestingLevel"]));
            }
            form.add(el, this.tr("Nesting level"), null, "nestingLevel");

            //load custom am components
            sm.lang.Object.forEachClass(function(clazz) {
                if (typeof clazz.getDescription === "function" &&
                        typeof clazz.applicableTo === "function" &&
                        qx.Class.hasInterface(clazz, ncms.asm.am.ICustomAMComponent) &&
                        clazz.applicableTo().indexOf("tree") !== -1) {
                    el = new sm.ui.form.IconCheckBox("ncms/icon/16/misc/gear.png");
                    el.setUserData("ccClass", clazz);
                    el.addListener("iconClicked", function(ev) {
                        this.__openAMCSettings(ev, attrSpec, asmSpec, opts);
                    }, this);
                    form.add(el, clazz.getDescription(), null, clazz.classname);
                }
            }, this);

            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        optionsAsJSON : function() {
            var opts = this._form.populateJSONObject({}, false, true);
            return opts;
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new ncms.asm.am.TreeAMValueWidget(asmSpec);
            this._fetchAttributeValue(attrSpec, function(val) {
                var opts = ncms.Utils.parseOptions(attrSpec["options"]);
                var model = qx.data.marshal.Json.createModel(JSON.parse(val), true);
                w.setModel(model);
                w.setOptions(opts);
            });
            this._valueWidget = w;
            return w;
        },


        valueAsJSON : function() {
            var w = this._valueWidget;
            var model = w.getModel();
            if (model == null) {
                return {};
            }
            model = qx.util.Serializer.toNativeObject(model);
            return model;
        },

        __openAMCSettings : function(ev, attrSpec, asmSpec, opts) {
            var w = ev.getTarget();
            var ccClass = w.getUserData("ccClass");
            if (ccClass == null) {
                return;
            }
            var wopts = opts[ccClass.classname];
            var dlg = ccClass.createOptionsDlg(attrSpec, asmSpec, wopts);
            dlg.addListener("completed", function(ev) {
                var data = ev.getData();
                qx.log.Logger.info("data=" + JSON.stringify(data));
                dlg.close();
            }, this);
            dlg.open();
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});