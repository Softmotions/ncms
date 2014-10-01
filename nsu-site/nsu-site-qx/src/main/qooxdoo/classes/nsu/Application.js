/**
 * Nsu site application
 */
qx.Class.define("nsu.Application", {
    extend : ncms.Application,

    members : {

        main : function() {
            this.base(arguments);
        },

        createActions : function() {
            return new nsu.Actions();
        }
    }
});