/**
 * Assembly attribute settings and value manager.
 */
qx.Interface.define("ncms.asm.IAsmAttributeManager", {

    members : {

        /**
         * Array of assembly attribute types supported by
         * this attribute manager.
         */
        getSupportedAttributeTypes : function() {
        },

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
         * @param attrSpec
         * @return {qx.ui.core.Widget} settings editor widget.
         */
        createOptionsWidget : function(attrSpec) {
            this.assertMap(attrSpec);
        },

        /**
         * Create attribute-value editor widget.
         * @return {qx.ui.core.Widget} value editor widget.
         */
        createValueEditorWidget : function(attrSpec) {
            this.assertMap(attrSpec);
        }
    }
});