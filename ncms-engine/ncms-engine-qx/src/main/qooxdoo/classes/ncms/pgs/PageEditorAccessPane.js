/**
 * Access rights pane of page editor tabbox.
 */
qx.Class.define("ncms.pgs.PageEditorAccessPane", {
    extend : qx.ui.tabview.Page,
    include : [ ncms.pgs.MPageEditorPane ],


    construct : function() {
        this.base(arguments, this.tr("Access rights"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.addListener("loadPane", this.__onLoadPane, this);

        this.__table = new ncms.pgs.PageEditorAccessTable();
        this.add(this.__table, {flex : 1});
    },

    members : {

        __table : null,

        __onLoadPane : function(ev) {
            var spec = ev.getData();

            this.__table.setPageSpec(spec);
        }

    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});