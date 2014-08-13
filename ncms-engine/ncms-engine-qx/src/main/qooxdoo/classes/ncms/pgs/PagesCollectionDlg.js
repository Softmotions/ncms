/**
 * User's pages collection dialog
 */
qx.Class.define("ncms.pgs.PagesCollectionDlg", {
    extend : qx.ui.window.Window,

    statics : {
    },

    events : {
        /**
         * Data: {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         */
        "completed" : "qx.event.type.Data"
    },

    construct : function(caption, options) {
        this._options = options || {};
        this.base(arguments, caption != null ? caption : this.tr("Page collection"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 620,
            height : 400
        });

        var table = new ncms.pgs.PagesCollectionTable(options);
        this.add(table, {flex : 1});

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = this._okBt = new qx.ui.form.Button(this.tr("Ok"));
        bt.addListener("execute", this._ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members : {


        _ok : function() {
            qx.log.Logger.info("ok");
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});