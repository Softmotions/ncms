/**
 * Default implementation of ncms.asm.am.IValueWidget
 */
qx.Mixin.define("ncms.asm.am.MValueWidget", {
    events: {
        /**
         * Fired if internal state of this widget modified by user input.
         */
        modified: "qx.event.type.Event",

        requestSave: "qx.event.type.Event"
    }
});