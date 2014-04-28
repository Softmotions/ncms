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


        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
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
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt = new qx.ui.menu.Button(this.tr("New assembly"));
            bt.addListenerOnce("execute", this.__onNewAssembly, this);
            menu.add(bt);

            var rind = this.__selector.getSelectedAsmInd();
            if (rind !== -1) {
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveAssembly, this);
                menu.add(bt);
            }
        },

        __onRemoveAssembly : function(ev) {
            var asm = this.__selector.getSelectedAsm();
            if (asm == null) {
                return;
            }
            ncms.Application.confirm(
                    this.tr("Are you sure to remove assembly: \"%1\"?", asm["name"]),
                    function(yes) {
                        if (!yes) return;
                        var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("asms", {id : asm["id"]}), "DELETE");
                        req.send(function(resp) {
                            this.__selector.reload();
                        }, this);
                    }, this);
        },

        __onNewAssembly : function(ev) {
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("asms.new"), "PUT", "application/json");
            req.send(function(resp) {
                var spec = resp.getContent();
                qx.log.Logger.info("spec=" + JSON.stringify(spec));
                this.__selector.getSearchField().setValue(spec["name"]);
            }, this);
        }
    },

    destruct : function() {
        this.__selector = null;
    }
});