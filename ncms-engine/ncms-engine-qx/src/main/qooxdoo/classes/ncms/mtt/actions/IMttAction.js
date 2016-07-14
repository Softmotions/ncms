/**
 * Mtt action interface
 */
qx.Interface.define("ncms.mtt.actions.IMttAction", {

//   statics : {
//
//        /**
//         * Returns short human readable filter description.
//         * @return {String}
//         */
//        getDescription : function() {
//        },
//
//        /**
//         * Returns a filter type
//         * @return {String}
//         */
//        getType : function() {
//        }
//
//        /**
//         * Convert action specification to human readable string.
//         * @param spec {Object}
//         */
//         specForHuman: function (spec) {
//         }
//    },

    members: {

        /**
         * Activate action options widget
         * @param spec {Object} Action specification
         */
        createWidget: function (spec) {
        },

        /**
         * Return rule specification JSON object
         * @param widget {qx.ui.core.Widget} Action widget created by `createWidget`
         */
        asSpec: function (widget) {
            this.assertNotNull(widget);
        }
    }
});