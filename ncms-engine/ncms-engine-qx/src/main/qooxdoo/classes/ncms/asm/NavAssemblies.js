/**
 * Left side navigation bar
 * controls all application assemblies
 * and assembly editor workspace.
 */
qx.Class.define("ncms.asm.NavAssemblies", {
    extend : qx.ui.core.Widget,

    statics : {
        ASM_EDITOR_CLAZZ : "ncms.asm.AsmEditor"
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.__selector = new ncms.asm.AsmSelector();
        this.__selector.addListener("asmSelected", this.__asmSelected, this);
        this._add(this.__selector);

        //Register assembly instance editor
        var eclazz = ncms.asm.NavAssemblies.ASM_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.asm.AsmEditor();
        }, null, this);

        this.addListener("disappear", function() {
            //Navigation side is inactive so hide assembly editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
        }, this);
        this.addListener("appear", function() {
            if (app.getActiveWSAID() != eclazz && this.__selector.getSelectedAsm() != null) {
                app.showWSA(eclazz);
            }
        }, this);
    },

    members : {

        __selector : null,

        __asmSelected : function(ev) {
            var data = ev.getData();
            var app = ncms.Application.INSTANCE;
            var eclazz = ncms.asm.NavAssemblies.ASM_EDITOR_CLAZZ;
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            app.getWSA(eclazz).setAsmId(data["id"]);
            app.showWSA(eclazz);
        }
    },

    destruct : function() {
        this.__selector = null;
    }
});