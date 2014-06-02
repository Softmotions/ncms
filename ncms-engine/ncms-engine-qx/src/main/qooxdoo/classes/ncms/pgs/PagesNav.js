/**
 * Pages selector.
 */
qx.Class.define("ncms.pgs.PagesNav", {
    extend : qx.ui.core.Widget,

    statics : {
        PAGE_EDITOR_CLAZZ : "ncms.pgs.PagesEditor"
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.__selector = new ncms.pgs.PagesTreeSelector(true);
        this._add(this.__selector);
    },

    members : {

        /**
         * Pages selector
         */
        __selector : null

    },

    destruct : function() {
        this.__selector = null;
        //this._disposeObjects("__field_name");                                
    }
});