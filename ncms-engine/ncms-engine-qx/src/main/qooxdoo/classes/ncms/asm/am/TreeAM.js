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
        },

        NESTED_AMS : [
            ncms.asm.am.RichRefAM
        ]
    },


    members : {

        _form : null,

        _opts : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            //attrSpec = sm.lang.Object.shallowClone(attrSpec);
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

            el = new qx.ui.form.Spinner(0, 1, 5);
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
                el.addListener("iconClicked", function(ev) {
                    this.__openNAMCSettings(ev, attrSpec, asmSpec);
                }, this);
                form.add(el, clazz.getDescription(), null, clazz.classname);
            }

            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        optionsAsJSON : function() {
            var opts = this._form.populateJSONObject({}, false, true);
            var items = this._form.getItems();
            for (var k in items) {
                /*var w = items[k];
                 var ccClass = w.getUserData("ccClass");
                 if (ccClass == null) {
                 continue;
                 }
                 opts[ccClass.classname] = JSON.stringify(this._opts[ccClass.classname]);*/
            }
            qx.log.Logger.info("optionsAsJSON=" + JSON.stringify(opts));
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

        __openNAMCSettings : function(ev, attrSpec, asmSpec) {
            var w = ev.getTarget();
            var clazz = w.getUserData("naClass");
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            attrSpec = sm.lang.Object.shallowClone(attrSpec);
            attrSpec["options"] = opts[clazz.classname];
            var dlg = new ncms.asm.am.AMWrapperDlg(clazz, attrSpec, asmSpec, {
                mode : "options"
            });
            dlg.addListener("completed", function(ev) {
                var data = ev.getData();
                qx.log.Logger.info("completed=" + JSON.stringify(data));
                //todo
                dlg.close();
            }, this);
            dlg.open();
        }
    },

    destruct : function() {
        this._opts = null;
        this._disposeObjects("_form");
    }
});