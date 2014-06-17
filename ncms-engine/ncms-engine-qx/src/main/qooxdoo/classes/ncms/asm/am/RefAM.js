/**
 * Assembly reference attribute manager
 *
 * See java side AM: com.softmotions.ncms.asm.am.AsmRefAttributeManager.
 *
 * @asset(ncms/icon/16/misc/document-import.png)
 */
qx.Class.define("ncms.asm.am.RefAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Assembly include");
        },

        getSupportedAttributeTypes : function() {
            return [ "asmref" ];
        }
    },

    members : {

        _form : null,

        _attrSpec : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var bf = new sm.ui.form.ButtonField(this.tr("Select assembly"),
                    "ncms/icon/16/misc/document-import.png");
            bf.setPlaceholder(this.tr("Please select template to include"));
            bf.setReadOnly(true);
            bf.addListener("execute", function() {
                this.__onSelectAssembly(attrSpec, asmSpec);
            }, this);
            this._fetchAttributeValue(attrSpec, function(val) {
                bf.setValue(val);
            });
            bf.setRequired(true);
            form.add(bf, this.tr("Assembly"), null, "assembly");

            this._form = form;
            return new sm.ui.form.FlexFormRenderer(form);
        },

        __onSelectAssembly : function(attrSpec, asmSpec) {
            var dlg = new ncms.asm.AsmSelectorDlg(
                    this.tr("Select assembly to include"), null,
                    {type : "", exclude : asmSpec["id"]},
                    ["name", "description"]);
            dlg.open();
            dlg.addListener("completed", function(ev) {
                var data = ev.getData();
                qx.log.Logger.info("data=" + JSON.stringify(data));
                dlg.close();
            }, this);
        },

        optionsAsJSON : function() {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            return {};
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            return new qx.ui.core.Widget();
        },

        valueAsJSON : function() {
            return {};
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});