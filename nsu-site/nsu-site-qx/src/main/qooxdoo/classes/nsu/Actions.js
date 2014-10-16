qx.Class.define("nsu.Actions", {
    extend : ncms.Actions,

    construct : function() {
        this.base(arguments, ""); //todo configurable prefix

        this._action("nsu.legacy.import", "/rs/adm/legacy/import/{id}");
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});