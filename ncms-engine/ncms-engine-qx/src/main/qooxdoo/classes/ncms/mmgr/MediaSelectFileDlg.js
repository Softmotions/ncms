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
            height : 450
        });

        var vsp = new qx.ui.splitpane.Pane("horizontal");
        var folders = new ncms.mmgr.MediaItemTreeSelector(allowModify);

        var leftSide = new qx.ui.container.Composite(new qx.ui.layout.VBox(5));
        leftSide.add(folders, {flex : 1});


        var rightSide = new qx.ui.container.Composite(new qx.ui.layout.VBox());
        var files = new ncms.mmgr.MediaFilesSelector(allowModify);
        folders.bind("itemSelected", files, "item");
        rightSide.add(files, {flex : 1});

        //Bottom buttons
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        hcont.setPadding(5);

        var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Ok")).set({enabled : false});
        bt.addListener("execute", this._ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        rightSide.add(hcont);


        var thumbnail = new qx.ui.basic.Image().set(
                {allowGrowX : false, allowGrowY : false,
                    allowShrinkX : false, allowShrinkY : false,
                    alignX : "center",
                    alignY : "middle",
                    maxHeight : 128, maxWidth : 128,
                    scale : true});

        leftSide.add(thumbnail);
        thumbnail.exclude();

        vsp.add(leftSide, 1);
        vsp.add(rightSide, 3);
        this.add(vsp, {flex : 1});

        this.__closeCmd = new qx.ui.core.Command("Esc");
        this.__closeCmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);

        files.addListener("fileSelected", function(ev) {
            var spec = ev.getData();
            var ctype = spec ? spec["content_type"] : null;
            if (ncms.Utils.isImageContentType(ctype)) {
                thumbnail.setSource(ncms.Application.ACT.getRestUrl("media.thumbnail2", spec));
                thumbnail.show();
            } else {
                thumbnail.exclude();
            }
        });
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
