/**
 * Nsu site application
 */
qx.Class.define("nsu.Application", {
    extend : qx.application.Standalone,
    include : [qx.locale.MTranslation],


    members : {

        main : function() {

        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});