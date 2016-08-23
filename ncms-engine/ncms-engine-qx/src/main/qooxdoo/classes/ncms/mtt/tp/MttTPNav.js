/**
 * Tracking pixels navigation side.
 */
qx.Class.define("ncms.mtt.tp.MttTPNav", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.MCommands],

    statics: {
        MTT_EDITOR_CLAZZ: "ncms.mtt.tp.MttTPEditor"
    },

    construct: function () {
        var me = this;
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());
        this.setPaddingLeft(10);


        // todo selector

        var eclazz = ncms.mtt.tp.MttTPNav.MTT_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.mtt.tp.MttTPEditor();
        }, null, this);


        this.addListener("disappear", function () {
            //Navigation side is inactive so hide mtt editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
            app.disposeWSA(eclazz);
        }, this);

        this.addListener("appear", function () {
            if (app.getActiveWSAID() != eclazz /*&& this.__selector.getSelectedRule() != null*/) {
                app.showWSA(eclazz);
            }
        }, this);


        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

        //this._registerCommand(
        //    new sm.ui.core.ExtendedCommand("Delete"),
        //    this.__onRemoveRule, this);
        //this._registerCommandFocusWidget(this.__selector.getTable());
    },

    members: {

        __selector: null,

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
        }
    },

    destruct: function () {
        this.__selector = null;
    }

});
