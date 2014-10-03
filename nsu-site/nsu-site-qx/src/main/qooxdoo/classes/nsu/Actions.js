qx.Class.define("nsu.Actions", {
    extend : ncms.Actions,

    construct : function() {
        this.base(arguments);

        this._action("nsu.legacy.import", "/rs/adm/legacy/import/{id}");
    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});