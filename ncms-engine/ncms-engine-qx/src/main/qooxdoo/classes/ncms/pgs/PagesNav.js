/**
 * Pages selector.
 */
qx.Class.define("ncms.pgs.PagesNav", {
    extend : ncms.pgs.PagesSelector,

    statics : {
        PAGE_EDITOR_CLAZZ : "ncms.pgs.PagesEditor"
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments, true);
        this.set({paddingTop : 5});
    },

    members : {


    },

    destruct : function() {
    }
});