/**
 * Left side navigation bar
 * controls all application assemblies
 * and assembly editor workspace.
 */
qx.Class.define("ncms.asm.AsmNav", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.MCommands],

    statics: {
        ASM_EDITOR_CLAZZ: "ncms.asm.AsmEditor"
    },

    events: {},

    properties: {},

    construct: function () {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.setPaddingLeft(10);
        this.__selector = new ncms.asm.AsmSelector();
        this.__selector.addListener("asmSelected", this.__asmSelected, this);
        this._add(this.__selector);

        //Register assembly instance editor
        var eclazz = ncms.asm.AsmNav.ASM_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.asm.AsmEditor();
        }, null, this);

        this.addListener("appear", function () {
            if (app.getActiveWSAID() != eclazz) {
                if (this.__selector.getSelectedAsm() != null) {
                    app.showWSA(eclazz);
                } else {
                    app.showDefaultWSA();
                }
            }
        }, this);


        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNewAssembly, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onRemoveAssembly, this);
        this._registerCommandFocusWidget(this.__selector.getTable());

    },

    members: {

        __selector: null,

        __asmSelected: function (ev) {
            var data = ev.getData();
            var app = ncms.Application.INSTANCE;
            var eclazz = ncms.asm.AsmNav.ASM_EDITOR_CLAZZ;
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            app.getWSA(eclazz).setAsmId(data["id"]);
            app.showWSA(eclazz);
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt = new qx.ui.menu.Button(this.tr("New assembly"));
            bt.addListenerOnce("execute", this.__onNewAssembly, this);
            menu.add(bt);

            bt = new qx.ui.menu.Button(this.tr("Refresh"));
            bt.addListenerOnce("execute", this.__onRefresh, this);
            menu.add(bt);

            var rind = this.__selector.getSelectedAsmInd();
            if (rind !== -1) {
                bt = new qx.ui.menu.Button(this.tr("Rename"));
                bt.addListenerOnce("execute", this.__onRenameAssembly, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveAssembly, this);
                menu.add(bt);
            }
        },

        __onRemoveAssembly: function (ev) {
            var asm = this.__selector.getSelectedAsm();
            if (asm == null) {
                return;
            }
            ncms.Application.confirm(
                this.tr("Are you sure to remove assembly: \"%1\"?", asm["name"]),
                function (yes) {
                    if (!yes) return;
                    var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("asms.delete", {id: asm["id"]}), "DELETE");
                    req.send(function (resp) {
                        this.__selector.reload();
                    }, this);
                }, this);
        },

        __onRenameAssembly: function (ev) {
            var asm = this.__selector.getSelectedAsm();
            if (asm == null) {
                return;
            }
            var dlg = new ncms.asm.AsmRenameDlg(asm["id"], asm["name"]);
            dlg.setPosition("bottom-right");
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this.__selector.reload();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.show();

        },

        __onRefresh: function () {
            this.__selector.reload();
        },

        __onNewAssembly: function (ev) {
            var dlg = new ncms.asm.AsmNewDlg();
            dlg.setPosition("bottom-right");
            dlg.addListenerOnce("completed", function (ev) {
                dlg.close();
                var spec = ev.getData();
                this.__selector.setSearchBoxValue(spec["name"]);
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(this.__selector.getTable(), false);
            }
            dlg.show();
        }
    },

    destruct: function () {
        this.__selector = null;
    }
});