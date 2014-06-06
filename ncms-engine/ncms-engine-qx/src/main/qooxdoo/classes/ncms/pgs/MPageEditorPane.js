/**
 * Common page editor tabs code.
 */
qx.Mixin.define("ncms.pgs.MPageEditorPane", {

    events : {
        /**
         * Fired if tab should be popuulated by page specific data
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