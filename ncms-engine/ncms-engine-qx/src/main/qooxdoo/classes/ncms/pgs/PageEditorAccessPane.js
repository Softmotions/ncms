/**
 * Access rights pane of page editor tabbox.
 */
qx.Class.define("ncms.pgs.PageEditorAccessPane", {
    extend : qx.ui.tabview.Page,
    include : [ ncms.pgs.MPageEditorPane ],


    construct : function() {
        this.base(arguments, this.tr("Access rights"));
        this.setLayout(new qx.ui.layout.VBox(5).set({alignX : "center"}));
        this.addListener("loadPane", this.__onLoadPane, this);

        this.add(new qx.ui.basic.Label(this.tr("Local")).set({font : "bold"}));
        var lat = this.__locAclTable = new ncms.pgs.PageEditorAccessTable({recursive : false});
        this.add(this.__locAclTable, {flex : 1});

        this.add(new qx.ui.basic.Label(this.tr("Recursive")).set({font : "bold"}));
        var rat =this.__recAclTable = new ncms.pgs.PageEditorAccessTable({recursive : true});
        this.add(this.__recAclTable, {flex : 1});

        var reload = function() {
            lat.reload();
            rat.reload();
        }
        this.__locAclTable.addListener("aclUpdated", reload, this);
        this.__recAclTable.addListener("aclUpdated", reload, this);
    },

    members : {

        __locAclTable : null,
        __recAclTable : null,

        __onLoadPane : function(ev) {
            var spec = ev.getData();
            this.__locAclTable.setPageSpec(spec);
            this.__recAclTable.setPageSpec(spec);
        },

        _applyModified : function(val) {
        }

    },

    destruct : function() {
        this.__locAclTable = null;
        this.__recAclTable = null;
    }
});