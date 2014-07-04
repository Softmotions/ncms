/**
 * Tree item for ncms.asm.am.TreeAMValueWidget
 */
qx.Class.define("ncms.asm.am.TreeAMItem", {
    extend : qx.ui.tree.VirtualTreeItem,

    properties : {

        extra : {
            check : "String",
            event : "changeExtra",
            nullable : true
        }
    },

    construct : function() {
        this.base(arguments);

    },

    members : {

        _addWidgets : function() {
            this.addSpacer();
            this.addOpenButton();
            this.addIcon();
            this.addLabel();
            this.addWidget(new qx.ui.core.Spacer(), {flex : 1});
            var extra = new qx.ui.basic.Label();
            this.bind("extra", extra, "value");
            this.addWidget(extra);
            this.addWidget(new qx.ui.core.Spacer().set({width : 70}));
        }
    }
});