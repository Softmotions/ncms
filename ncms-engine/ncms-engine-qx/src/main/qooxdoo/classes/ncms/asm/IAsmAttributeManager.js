/**
 * Assembly attribute settings and value manager.
 *
 * NOTE:
 * Implementors must define the following static methods:
 *
 *  statics : {
 *
 *      getDescription() {
 *      }
 *
 *      getMetaInfo : function() {
 *      }
 *  }
 *
 *  //todo describe 'widgetNotReady' state
 *  //todo describe 'ncms.asm.activeWidget' widget user data
 *  //todo describe 'ncms.asm.validator' user data
 *
 */
qx.Interface.define("ncms.asm.IAsmAttributeManager", {


    //statics: {
    //
    //    // {String} Assembly description
    //    getDescription() {
    //    }
    //
    //    /**
    //     * Return attribute descriptor object:
    //     *
    //     * {
    //     *   attributeTypes: {Array|String} String array of attribute types supported by this attribute manager
    //     *   hidden: {Boolean?false} If this attribute is hidden from UI
    //     *   requiredSupported: {Boolean?false} If this attribute supports a `required` mode. (Required field)
    //     *   unique: {Boolean?false} If this attribute must be unique for assembly.
    //     * }
    //     */
    //
    //    getMetaInfo : function() {
    //    }
    //},

    members: {

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
         * @param asmSpec {Object}
         * @return {qx.ui.core.Widget} settings editor widget.
         */
        activateOptionsWidget: function (attrSpec, asmSpec) {
            this.assertMap(attrSpec);
            this.assertMap(asmSpec);
        },

        /**
         * @return {Object}
         */
        optionsAsJSON: function () {
        },

        /**
         * Create attribute-value editor widget.
         *
         * @param attrSpec {Object}
         * @param asmSpec {Object}
         * @return {qx.ui.core.Widget} value editor widget.
         */
        activateValueEditorWidget: function (attrSpec, asmSpec) {
            this.assertMap(attrSpec);
            this.assertMap(asmSpec);
        },

        /**
         * @return {Object}
         */
        valueAsJSON: function () {
        }
    }
});