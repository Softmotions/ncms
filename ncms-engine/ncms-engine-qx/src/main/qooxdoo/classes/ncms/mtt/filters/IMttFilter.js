/**
 * MttFilter interface
 */
qx.Interface.define("ncms.mtt.filters.IMttFilter", {

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
//        getFilterType : function() {
//        }
//    },

    members: {

        /**
         * Activate filter options widget
         * @param filterSpec Filter specification
         */
        activateFilterWidget: function (filterSpec) {
            this.assertMap(filterSpec);
        },

        /**
         * Return filter specification JSON object
         * @param filterWidget Filter widget created by `activateFilterWidget`
         */
        filterAsSpec: function (filterWidget) {
        }
    }

});