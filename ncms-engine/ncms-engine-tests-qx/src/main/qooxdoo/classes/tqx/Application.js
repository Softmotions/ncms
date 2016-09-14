/**
 * Nsu site application.
 */
qx.Class.define("tqx.Application", {
    extend: ncms.Application,

    members: {

        main: function () {
            this.base(arguments);
        },

        createActions: function () {
            return new tqx.Actions();
        }
    }
});