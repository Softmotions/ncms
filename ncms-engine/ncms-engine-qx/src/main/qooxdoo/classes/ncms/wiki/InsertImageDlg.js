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
            var caption = new qx.ui.form.TextArea().set({maxLength : 256, minimalLineHeight : 2});
            form.add(caption, this.tr("Caption"), null, "caption");

            var rg = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            rg.add(new qx.ui.form.RadioButton(this.tr("Original")).set({"model" : "original"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("Small")).set({"model" : "small"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("Medium")).set({"model" : "medium"}));
            rg.add(new qx.ui.form.RadioButton(this.tr("Large")).set({"model" : "large"}));
            form.add(rg, this.tr("Size"), null, "size");
        },

        _createFormRenderer : function(form) {
            return new sm.ui.form.FlexFormRenderer(form);
        }

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});