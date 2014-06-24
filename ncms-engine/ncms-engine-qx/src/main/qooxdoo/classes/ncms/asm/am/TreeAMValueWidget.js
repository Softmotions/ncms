/**
 * Tree value editor widget.
 */
qx.Class.define("ncms.asm.am.TreeAMValueWidget", {
    extend : qx.ui.container.Composite,

    statics : {
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this.setLayout(new qx.ui.layout.VBox());
        this.getChildControl("toolbar");

    },

    members : {

        _createChildControlImpl : function(id) {
            var control;
            switch (id) {
                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar();
                    this._createToolbarItems(control);
                    this.add(control);
                    break;
            }
            return control || this.base(arguments, id);
        },


        _createToolbarItems : function(toolbar) {

        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});
