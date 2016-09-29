/**
 * Attribute editor dialog
 *
 * @asset(ncms/icon/16/misc/application-form.png)
 */
qx.Class.define("ncms.asm.AsmAttrEditorDlg", {
    extend: qx.ui.window.Window,

    statics: {},

    events: {
        /**
         * Data: assembly attribute JSON representation.
         * Example:
         *
         *  {
         *    "name" : "foo",
         *    "type" : "string",
         *    "label" : null,
         *    "asmId" : 2,
         *    "options" : {"display" : "field", "value" : "some text"},
         *    "required" : true
         *  }
         */
        "completed": "qx.event.type.Data"
    },

    properties: {},


    construct: function (caption, asmSpec, attrSpec) {
        this.base(arguments, caption != null ? caption : this.tr("Edit assembly attribute"));
        this.setLayout(new qx.ui.layout.VBox(5, "top", "separator-vertical"));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 400
        });
        qx.core.Assert.assertMap(asmSpec, "Missing 'asmSpec' constructor argument");

        var amClazz = (attrSpec != null) ?
                      ncms.asm.am.AsmAttrManagersRegistry.findEditorClassForType(attrSpec["type"]) : null;

        this.__attrSpec = attrSpec;
        this.__asmSpec = asmSpec;

        //-------------- Main attribute properties

        var form = this.__form = new sm.ui.form.ExtendedForm();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        //name
        var el = new qx.ui.form.TextField();
        el.setRequired(true);
        el.setMaxLength(127);
        if (attrSpec != null) {
            el.setValue(attrSpec["name"]);
        }
        form.add(el, this.tr("Name"), null, "name");

        //Attribute type
        el = new sm.ui.form.ButtonField(this.tr("Type"), "ncms/icon/16/misc/application-form.png");
        el.setPlaceholder(this.tr("Choose the attribute type"));
        el.setReadOnly(true);
        el.setRequired(true);
        if (attrSpec != null) {
            el.setValue(attrSpec["type"]);
        }
        el.addListener("execute", this.__selectType, this);
        form.add(el, this.tr("Type"), null, "type");

        //GUI label
        el = new qx.ui.form.TextField();
        el.setMaxLength(64);
        if (attrSpec != null) {
            el.setValue(attrSpec["label"])
        }
        form.add(el, this.tr("GUI Label"), null, "label");

        //GUI required checkbox
        el = new qx.ui.form.CheckBox(this.tr("Required"));
        if (attrSpec != null) {
            el.setValue(!!attrSpec["required"]);
        }
        form.add(el, this.tr("GUI Required"), null, "required");


        var fr = new sm.ui.form.FlexFormRenderer(form);
        fr.setPaddingBottom(10);
        this.add(fr);

        //---------------- Type-specific editor placeholder

        this.__typeEditorStack = this.__createTypeWidgetStack();
        this.__typeEditorStack.setPaddingBottom(10);
        this.add(this.__typeEditorStack, {flex: 1});

        //----------------- Footer

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);

        if (amClazz != null) {
            this.__setType(attrSpec["type"], amClazz);
        } else {
            qx.event.Timer.once(function () {
                this.__selectType();
            }, this, 0);
        }
    },

    members: {

        __form: null,

        __attrSpec: null,

        __asmSpec: null,

        __typeEditorStack: null,

        __getAttributeSpec: function () {
            return this.__attrSpec || {};
        },

        __createTypeWidgetStack: function () {
            var ts = new sm.ui.cont.LazyStack();
            ts.setWidgetsHidePolicy("exclude");
            var me = this;
            ts.setOnDemandFactoryFunctionProvider(function () {
                return function (id) {
                    var editor = ncms.asm.am.AsmAttrManagersRegistry.createAttrManagerInstance(id);
                    var aspec = me.__getAttributeSpec();
                    var w = editor.activateOptionsWidget(aspec, me.__asmSpec);
                    if (w == null) {
                        w = new qx.ui.core.Widget();
                    }
                    w.setUserData("editor", editor);
                    return w;
                }
            });
            return ts;
        },

        __selectType: function () {
            var dlg = new ncms.asm.AsmAttributeTypeSelectorDlg();
            dlg.addListenerOnce("completed", function (ev) {
                var data = ev.getData();
                dlg.destroy();
                //Data: [type, editor_clazz]
                this.__setType(data[0], data[1]);
            }, this);
            dlg.show();
        },

        __setType: function (type, editorClazz) {
            var items = this.__form.getItems();
            var meta = editorClazz.getMetaInfo() || {};
            items["type"].setValue(type);
            items["label"].setEnabled(!meta.hidden);
            items["required"].setEnabled(!meta.hidden && meta.requiredSupported);
            if (!meta.hidden) {
                items["label"].show();
            } else {
                items["label"].exclude();
            }
            if (meta.requiredSupported && !meta.hidden) {
                items["required"].show();
            } else {
                items["required"].exclude();
            }
            if (sm.lang.String.isEmpty(items["name"].getValue())) {
                items["name"].setValue(type);
            }
            this.__typeEditorStack.showWidget(editorClazz.classname);
        },

        __ok: function () {
            if (!this.__form.validate()) {
                return;
            }
            var w = this.__typeEditorStack.getActiveWidget();
            if (w == null) {
                return;
            }
            var editor = w.getUserData("editor");
            if (editor == null) {
                return;
            }
            var optsJson = editor.optionsAsJSON();
            if (optsJson == null) { //attribute manager prohibited farther saving and reported error
                return;
            }
            var attrSpec = this.__attrSpec || {};
            var sobj = this.__form.populateJSONObject({});
            sobj["asmId"] = attrSpec["asmId"] || this.__asmSpec["id"];
            sobj["old_name"] = attrSpec["name"];
            sobj["old_type"] = attrSpec["type"];
            sobj["options"] = optsJson;

            var req = ncms.Application.request("asms.attributes", {id: sobj["asmId"]}, "PUT");
            req.setRequestContentType("application/json");
            req.setData(JSON.stringify(sobj));
            req.send(function (resp) {
                this.fireDataEvent("completed", sobj);
            }, this);
        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        if (this.__typeEditorStack) {
            this.__typeEditorStack.getActivatedWidgets().forEach(function (w) {
                var editor = w.getUserData("eidtor");
                if (editor) {
                    editor.dispose();
                }
            });
        }
        this._disposeObjects("__form");
        this.__asmSpec = null;
        this.__attrSpec = null;
        this.__typeEditorStack = null;
    }
});