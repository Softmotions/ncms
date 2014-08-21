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
        },

        createOptionsDlg : function(attrSpec, asmSpec, options) {
            return new ncms.asm.am.RichResourceRefAMCOptsDlg(attrSpec, asmSpec, options);
        }
    },

    events : {

        "completed" : "qx.event.type.Data"
    },

    members : {

        open : function(attrSpec, asmSpec, options) {
        },

        close : function() {
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});