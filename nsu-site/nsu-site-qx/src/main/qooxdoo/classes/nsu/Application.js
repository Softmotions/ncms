/**
 * Nsu site application
 */
qx.Class.define("nsu.Application", {
    extend : ncms.Application,

    members : {

        main : function() {
            this.base(arguments);
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});