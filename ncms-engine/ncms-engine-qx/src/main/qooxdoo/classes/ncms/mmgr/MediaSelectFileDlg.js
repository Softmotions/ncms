/**
 * Select file dialog.
 */
qx.Class.define("ncms.mmgr.MediaSelectFileDlg", {
    extend : qx.ui.window.Window,

    events : {
        /**
         * Data example:
         *
         * {"id":2,
         *   "name":"496694.png",
         *   "folder":"/test/",
         *   "content_type":"image/png",
         *   "content_length":10736,
         *   "creator" : "adam",
         *   "tags" : "foo, bar"
         *   }
         * or null
         */
        "completed" : "qx.event.type.Data"
    },

    construct : function(allowModify, caption, icon) {
        allowModify = !!allowModify;
        this.base(arguments, caption, icon);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            width : 620,
            height : 400
        });

        var vsp = new qx.ui.splitpane.Pane("horizontal");
        var folders = new ncms.mmgr.MediaItemTreeSelector(allowModify);
        vsp.add(folders, {flex : 1});

        var files = new ncms.mmgr.MediaFilesSelector(allowModify);
        folders.bind("itemSelected", files, "item");
        vsp.add(files, {flex : 3});
        this.add(vsp, {flex : 1});

        //Botm buttons
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Ok")).set({enabled : false});
        bt.addListener("execute", this._ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        this.__closeCmd = new qx.ui.core.Command("Esc");
        this.__closeCmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);

        //fixme qooxdoo bug workaround!
        this.addListenerOnce("appear", function() {
            folders.setLayoutProperties({ flex : 1 });
            files.setLayoutProperties({ flex : 3 });
        }, this);
    },

    members : {

        __folders : null,

        __saveBt : null,

        __closeCmd : null,

        close : function() {
            this.base(arguments);
            this.__dispose();
        },

        __dispose : function() {
            if (this.__closeCmd) {
                this.__closeCmd.setEnabled(false);
            }
            this._disposeObjects("__closeCmd");
            this.__saveBt = null;
            this.__folders = null;
        }
    },

    destruct : function() {
        this.__dispose();
        //this._disposeObjects("__field_name");
    }
});
