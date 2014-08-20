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
         * Page publish status changed.
         *
         * Data:
         * {
         *  id : {Number} Page ID,
         *  published : {Boolean} Is page published
         * }
         */
        "pageChangePublished" : "qx.event.type.Data",


        /**
         * Page template changed.
         *
         * Data:
         * {
         *  id : {Number} Page ID,
         *  templateId : {Number} Page template ID
         * }
         *
         */
        "pageChangeTemplate" : "qx.event.type.Data",


        /**
         * Basic assembly properties changed in the assembly editor GUI.
         */
        "asmPropsChanged" : "qx.event.type.Data"
    }

});