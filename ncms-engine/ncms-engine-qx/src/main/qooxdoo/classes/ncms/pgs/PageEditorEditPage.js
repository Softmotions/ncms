/**
 * Edit page tab of page editor tabbox.
 */
qx.Class.define("ncms.pgs.PageEditorEditPage", {
    extend : qx.ui.tabview.Page,
    include : [ ncms.pgs.MPageEditorPane ],


    construct : function() {
        this.base(arguments, this.tr("Edit"));
        this.setLayout(new qx.ui.layout.VBox(5, "middle"));
        this.add(new qx.ui.basic.Label(this.tr("Loading...")).set({alignX : "center"}));

        this.addListener("loadPane", this.__onLoadPane, this);
    },

    members : {

        __onLoadPane : function(ev) {
            var spec = ev.getData();
            qx.log.Logger.info("Editor load pane=" + spec);
        }

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});