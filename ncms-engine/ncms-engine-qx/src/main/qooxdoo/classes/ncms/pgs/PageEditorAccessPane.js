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

        this.add(new qx.ui.basic.Label(this.tr("Local")));
        this.__locAclTable = new ncms.pgs.PageEditorAccessTable({recursive : false});
        this.add(this.__locAclTable, {flex : 1});

        this.add(new qx.ui.basic.Label(this.tr("Recursive")));
        this.__recAclTable = new ncms.pgs.PageEditorAccessTable({recursive : true});
        this.add(this.__recAclTable, {flex : 1});
    },

    members : {

        __locAclTable : null,
        __recAclTable : null,

        __onLoadPane : function(ev) {
            var spec = ev.getData();

            this.__locAclTable.setPageSpec(spec);
            this.__recAclTable.setPageSpec(spec);
        }

    },

    destruct : function() {
        this.__locAclTable = null;
        this.__recAclTable = null;
    }
});