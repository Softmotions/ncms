/**
 * Users selector pane.
 */
qx.Class.define("ncms.usr.UsersNav", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());

        //todo remove it
        this.setBackgroundColor("#ffffcc");

        //todo use example: ncms.asm.AsmSelector
        //todo use sm.ui.form.SearchField as search box
        //todo use ncms.usr.UsersTable (example: ncms.asm.AsmTable)
        //todo use com.softmotions.ncms.security.NcmsSecurityRS service

    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");                                
    }
});