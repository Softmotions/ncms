/**
 * App site application.
 */
qx.Class.define("${rootArtifactId}.Application", {
    extend: ncms.Application,

    members: {

        main: function () {
            this.base(arguments);
        },

        createActions: function () {
            return new ${rootArtifactId}.Actions();
        }
    }
});