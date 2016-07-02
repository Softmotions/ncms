/**
 * News editor main pane.
 */
qx.Class.define("ncms.news.NewsEditor", {
    extend: ncms.pgs.PageEditor,

    statics: {},

    events: {},

    properties: {},

    construct: function () {
        this.base(arguments);
    },

    members: {
        _createAccessPane: function () {
            return null;
        }
    },

    destruct: function () {
        //this._disposeObjects("__field_name");
    }
});