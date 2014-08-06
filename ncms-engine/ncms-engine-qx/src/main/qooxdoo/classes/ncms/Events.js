qx.Class.define("ncms.Events", {
    extend : qx.core.Object,
    type : "singleton",

    events : {

        /**
         * Data:
         * {
         *  id : {Number} page ID
         *  ...
         *  ...
         */
        "pageEdited" : "qx.event.type.Data"
    }

});