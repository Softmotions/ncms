/**
 * Rich resource reference custtom attributemanager component
 */
qx.Class.define("ncms.asm.am.RichResourceRefAMC", {
    extend : qx.core.Object,
    implement : [ ncms.asm.am.ICustomAMComponent ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Rich reference");
        },

        applicableTo : function() {
            return ["tree"];
        }
    },

    events : {

        "completed" : "qx.event.type.Data"
    },

    members : {

        createOptionsDlg : function(options) {
        },

        open : function() {
        },

        close : function() {
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});