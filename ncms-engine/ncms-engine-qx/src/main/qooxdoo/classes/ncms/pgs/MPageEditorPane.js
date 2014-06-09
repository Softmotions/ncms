/**
 * Common page editor tab-pane code.
 *
 * Supports lazy loading of pane content.
 * Pane data can loaded on `loadPane` event.
 */
qx.Mixin.define("ncms.pgs.MPageEditorPane", {

    events : {
        /**
         * Fired if pane should be populated by page specific data
         *
         * Data: pageSpec property value.
         */
        "loadPane" : "qx.event.type.Data",

        /**
         * Fired if pane should be cleared.
         */
        "clearPane" : "qx.event.type.Event"
    },

    properties : {

        /**
         * pageSpec:
         * {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         *
         * @see ncms.pgs.PageEditor
         */
        "pageSpec" : {
            check : "Object",
            nullable : true,
            apply : "__applyPageSpec"
        }
    },

    construct : function() {
        this.addListener("appear", function() {
            if (this.__stateDeffered) {
                this.__applyPageSpec(this.getPageSpec());
            }
        }, this);
    },

    members : {

        __stateDeffered : false,

        __applyPageSpec : function(spec) {
            if (!this.isVisible()) {
                this.__stateDeffered = true;
                return;
            }
            this.__stateDeffered = false;
            if (spec != null) {
                this.fireDataEvent("loadPane", spec);
            } else {
                this.fireEvent("clearPane");
            }
        }
    }
});