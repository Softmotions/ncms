/**
 * Basic contract for custom non-form value widget
 */
qx.Interface.define("ncms.asm.am.IValueWidget", {

    events: {
        /**
         * Fired if internal state of this widget modified by user input.
         */
        modified: "qx.event.type.Event",

        /**
         * Fire if need to save a the page
         */
        requestSave: "qx.event.type.Event"

    }
});