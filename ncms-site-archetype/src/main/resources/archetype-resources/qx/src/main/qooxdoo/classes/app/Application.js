/**
 * App site application.
 */
qx.Class.define("app.Application", {
    extend: ncms.Application,

    members: {

        main: function () {
            this.base(arguments);
        },

        createActions: function () {
            return new sml.Actions();
        }
    }
});