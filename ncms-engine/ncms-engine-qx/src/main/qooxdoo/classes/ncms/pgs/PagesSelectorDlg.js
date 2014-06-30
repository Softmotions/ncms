/**
 * Pages selector dialog.
 */
qx.Class.define("ncms.pgs.PagesSelectorDlg", {
    extend : qx.ui.window.Window,

    events : {
        /**
         * Data: {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         */
        "completed" : "qx.event.type.Data"
    },

    construct : function(caption, allowModify) {
        this.base(arguments, caption != null ? caption : this.tr("Select page"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 620,
            height : 400
        });

        var selector = this._selector = new ncms.pgs.PagesSelector(!!allowModify);
        this.add(selector, {flex : 1});

        this._initForm();

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = this._okBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this._ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        this.__closeCmd = new qx.ui.core.Command("Esc");
        this.__closeCmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);

        selector.addListener("pageSelected", this._syncState, this);
        this._syncState();
    },

    members : {

        _selector : null,

        _okBt : null,

        __closeCmd : null,

        _ok : function() {
            this.__selector.getSelectedPageWithExtraInfo(function(sp) {
                if (sp != null) {
                    this.fireDataEvent("completed", sp);
                }
            }, this);
        },

        _syncState : function() {
            this._okBt.setEnabled(this._selector.getSelectedPage() != null);
        },

        _initForm : function() {

        },

        close : function() {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct : function() {
        this._okBt = null;
        this._selector = null;
        this._disposeObjects("__closeCmd");
    }
});
