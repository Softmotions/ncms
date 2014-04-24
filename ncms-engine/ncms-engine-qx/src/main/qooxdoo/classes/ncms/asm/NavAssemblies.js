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
        var app = ncms.Application.INSTANCE;
        app.registerWSA(ncms.asm.NavAssemblies.ASM_EDITOR_CLAZZ, function() {
            return new ncms.asm.AsmEditor();
        }, null, this);

        this.addListener("disappear", function() {
            //Navigation side is inactive so hide assembly editor pane if it not done already
            if (app.getActiveWSAID() == ncms.asm.NavAssemblies.ASM_EDITOR_CLAZZ) {
                app.showDefaultWSA();
            }
        });
    },

    members : {

        __selector : null,

        __asmSelected : function(ev) {
            var data = ev.getData();
            var app = ncms.Application.INSTANCE;
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            qx.log.Logger.info("__asmSelected=" + JSON.stringify(data));
            app.showWSA(ncms.asm.NavAssemblies.ASM_EDITOR_CLAZZ);
        }
    },

    destruct : function() {
        this.__selector = null;
    }
});