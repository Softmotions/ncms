/**
 * Custom attribute manager GUI component.
 * Used in {@link ncms.asm.am.TreeAM}.
 *
 * NOTE:
 *  All implementors must have {@code getDescription()} static method
 *  to get a short implementation description/label.
 *
 *  And {@code applicableTo()} method returing an array of supported by this widget
 *  attribute manager types.
 *
 *  Implementor constructor may have one options argument.
 */
qx.Interface.define("ncms.asm.am.ICustomAMComponent", {

//    statics : {
//
//        getDescription : function() {
//        }
//
//        applicableTo : function() {
//        }
//
//        createOptionsDlg : function(attrSpec, asmSpec, options) {
//        }
//    },

    events : {

        /**
         * Fired with custom component data.
         */
        "completed" : "qx.event.type.Data"
    },

    members : {

        /**
         * Activate this component
         */
        open : function(attrSpec, asmSpec, options) {
        },

        /**
         * Dispose component
         */
        close : function() {
        }
    }
});