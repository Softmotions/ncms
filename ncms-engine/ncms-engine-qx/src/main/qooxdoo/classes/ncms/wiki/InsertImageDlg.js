/**
 * Insert image dialog
 */
qx.Class.define("ncms.wiki.InsertImageDlg", {
    extend : ncms.mmgr.PageFilesSelectorDlg,

    construct : function(pageId, caption) {
        this.base(arguments, pageId, caption, {noLinkText : true, allowModify : true});
        this.setCtypeAcceptor(ncms.Utils.isImageContentType.bind(ncms.Utils));
    },

    members : {

        _initForm : function(form) {
            var caption = new qx.ui.form.TextField().set({maxLength : 256});
            form.add(caption, this.tr("Caption"), null, "caption");

            var rg = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            rg.add(new qx.ui.form.RadioButton(this.tr("original")).set({"model" : "original"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("small")).set({"model" : "small"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("medium")).set({"model" : "medium"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("large")).set({"model" : "large"}));
            form.add(rg, this.tr("Size"), null, "size");

            rg = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            rg.add(new qx.ui.form.RadioButton(this.tr("default")).set({"model" : "none"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("center")).set({"model" : "center"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("left")).set({"model" : "left"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("right")).set({"model" : "right"}));
            form.add(rg, this.tr("Position"), null, "position");

        },

        _createFormRenderer : function(form) {
            return new sm.ui.form.FlexFormRenderer(form);
        }

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});