/**
 * Info page tab of page editor tabbox.
 */
qx.Class.define("ncms.pgs.PageEditorInfoPage", {
    extend : qx.ui.tabview.Page,
    include : [ ncms.pgs.MPageEditorPane ],


    construct : function() {
        this.base(arguments, this.tr("Info"));
        this.setLayout(new qx.ui.layout.VBox());
        this.addListener("loadPane", this.__onLoadPane, this);
    },

    members : {

        __alerBox : null,

        __onLoadPane : function(ev) {
            var spec = ev.getData();
            qx.log.Logger.info("Info load pane=" + JSON.stringify(spec));
        }
    },

    destruct : function() {
        this.__alertBox = null;
        //this._disposeObjects("__field_name");
    }
});