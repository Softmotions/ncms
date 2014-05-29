/**
 * Attribute editor dialog
 *
 * @asset(ncms/icon/16/misc/application-form.png)
 */
qx.Class.define("ncms.asm.AsmAttrEditorDlg", {
    extend : qx.ui.window.Window,

    statics : {
    },

    events : {
        /**
         * Data: assembly attribute JSON representation.
         */
        "completed" : "qx.event.type.Data"
    },

    properties : {
    },


    construct : function(caption, asmSpec, attrName) {
        this.base(arguments, caption != null ? caption : this.tr("Edit assembly attribute"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 620,
            height : 400
        });

        qx.core.Assert.assertMap(asmSpec, "Missing 'asmSpec' constructor argument");

        this.__attrName = attrName;
        this.__asmSpec = asmSpec;
        var attrSpec = null;

        if (attrName != null) {
            attrSpec = (asmSpec["effectiveAttributes"] || {})[attrName];
        }

        //-------------- Main attribute properties

        var form = this.__form = new qx.ui.form.Form();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        //name
        var el = new qx.ui.form.TextField();
        el.setRequired(true);
        el.setMaxLength(127);
        el.tabFocus();
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
        el.setMaxLength(32);
        form.add(el, this.tr("Label"), null, "label");

        var fr = new sm.ui.form.FlexFormRenderer(form);
        this.add(fr);

        //---------------- Type-specific editor placeholder

        this.__typeEditorStack = this.__createTypeWidgetStack();
        this.add(this.__typeEditorStack, {flex : 1});

        //----------------- Footer

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        this.__closeCmd = new qx.ui.core.Command("Esc");
        this.__closeCmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members : {

        __form : null,

        __closeCmd : null,

        __attrName : null,

        __asmSpec : null,

        __typeEditorStack : null,

        __getAttributeSpec : function() {
            return ((this.__asmSpec["effectiveAttributes"] || {})[this.__attrName]) || {};
        },

        __createTypeWidgetStack : function() {
            var ts = new sm.ui.cont.LazyStack();
            ts.setWidgetsHidePolicy("destroy");
            var me = this;
            ts.setOnDemandFactoryFunctionProvider(function() {
                return function(id) {
                    var editor = ncms.asm.AsmAttrManagersRegistry.createAttrManagerInstance(id);
                    var aspec = me.__getAttributeSpec();
                    return editor.createOptionsWidget(aspec);
                }
            });
            return ts;
        },

        __selectType : function() {
            var dlg = new ncms.asm.AsmAttributeTypeSelectorDlg();
            dlg.addListenerOnce("appear", function() {
                this.__closeCmd.setEnabled(false);
            }, this);
            dlg.addListenerOnce("disappear", function() {
                this.__closeCmd.setEnabled(true);
            }, this);
            dlg.addListener("completed", function(ev) {
                var data = ev.getData();
                dlg.hide();
                //Data: [type, editor clazz]
                this.__setType(data[0], data[1]);
            }, this);
            dlg.show();
        },

        __setType : function(type, editorClazz) {
            var items = this.__form.getItems();
            items["type"].setValue(type);
            this.__typeEditorStack.showWidget(editorClazz.classname);
        },

        __dispose : function() {
            if (this.__closeCmd) {
                this.__closeCmd.setEnabled(false);
            }
            this.__asmSpec = null;
            this.__attrName = null;
            this.__typeEditorStack = null;
            this._disposeObjects("__form", "__closeCmd", "__typeEditorStack");
        },

        __ok : function() {
            if (!this.__form.validate()) {
                return;
            }
        },

        close : function() {
            this.base(arguments);
            this.__dispose();
        }
    },

    destruct : function() {
        this.__dispose();
    }
});