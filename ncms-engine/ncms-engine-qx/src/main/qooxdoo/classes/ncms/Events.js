qx.Class.define("ncms.Events", {
    extend : qx.core.Object,
    type : "singleton",

    events : {

        /**
         * Page content edited.
         *
         * Data:
         * {
         *  id : {Number} Page ID
         *  ...
         *  ...
         */
        "pageEdited" : "qx.event.type.Data",


        /**
         * Page publishe status changed.
         *
         * Data:
         * {
         *  id : {Number} Page ID,
         *  published : {Boolean} Is page published
         *
         *
         */
        "pageChangePublished" : "qx.event.type.Data"
    }

});