/**
 * Assembly attribute settings and value manager.
 *
 * Implementors must define the following static methods:
 *
 *      getDescription
 *      getSupportedAttributeTypes
 *
 */
qx.Interface.define("ncms.asm.IAsmAttributeManager", {

    statics : {

        /**
         * Returns short human readable editor description.
         * @returns {String}
         */
        getDescription : function() {
        },

        /**
         * Return list of supported asm types by this editor
         * @returns {Array}
         */
        getSupportedAttributeTypes : function() {
        }
    },

    members : {

        /**
         * Return attribute-options editor widget.
         *
         * attrSpec example:
         * {
         *  "asmId" : 1,
         *  "name" : "copyright",
         *  "type" : "string",
         *  "value" : "My company (c)",
         *  "options" : null,
         *   "hasLargeValue" : false
         * }
         *
         * @param attrSpec {Object}
         * @return {qx.ui.core.Widget} settings editor widget.
         */
        activateOptionsWidget : function(attrSpec) {
            this.assertMap(attrSpec);
        },

        /**
         * @returns {Object}
         */
        optionsAsJSON : function() {
        },

        /**
         * Create attribute-value editor widget.
         *
         * @param attrSpec {Object}
         * @return {qx.ui.core.Widget} value editor widget.
         */
        activateValueEditorWidget : function(attrSpec) {
            this.assertMap(attrSpec);
        },

        valueAsJSON : function() {
        }
    }
});