/**
 * Select file dialog.
 *
 */
qx.Class.define("ncms.mmgr.MediaSelectFileDlg", {
    extend: qx.ui.window.Window,

    events: {
        /**
         * Array of selected files.
         * Data example:
         *
         * [ {"id":2,
         *   "name":"496694.png",
         *   "folder":"/test/",
         *   "content_type":"image/png",
         *   "content_length":10736,
         *   "owner" : "adam",
         *   "tags" : "foo, bar"
         *   },
         *   ...
         * ]
         */
        "completed": "qx.event.type.Data"
    },

    properties: {

        /**
         * Accept content type filter function.
         * Function signature: (ctype {String}) => Boolean
         */
        ctypeAcceptor: {
            "check": "Function",
            nullable: true
        }
    },

    /**
     * Options:
     * <code>
     *  {
     *      allowMove : {Boolean?false},
     *      allowSubfoldersView : {Boolean?false},
     *      pageSpec: {id: page id, name: page name} Optional page spec to show files in page
     *  }
     * </code>
     * @param allowModify {Boolean?false}
     * @param caption {String?} Dialog caption.
     * @param opts {Object?} ncms.mmgr.MediaFilesSelector options.
     */
    construct: function (allowModify, caption, opts) {
        allowModify = !!allowModify;
        this.base(arguments, caption);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            showStatusbar: true,
            width: 700,
            height: 450
        });

        opts = opts || {};

        var vsp = new qx.ui.splitpane.Pane("horizontal");
        var folders = new ncms.mmgr.MediaItemTreeSelector(allowModify);

        var leftSide = new qx.ui.container.Composite(new qx.ui.layout.VBox(5));
        leftSide.add(folders, {flex: 1});

        var rightSide = new qx.ui.container.Composite(new qx.ui.layout.VBox());
        var files = this.__files = new ncms.mmgr.MediaFilesSelector(allowModify, {status: 0}, opts);
        files.getTable().addListener("cellDbltap", this.__ok, this);
        folders.bind("itemSelected", files, "item");
        files.bind("inpage", folders, "enabled", {
            converter: function (v) {
                return !v;
            }
        });
        rightSide.add(files, {flex: 1});


        //Bottom buttons
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
        hcont.setPadding(5);

        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Ok")).set({enabled: false});
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        rightSide.add(hcont);


        var thumbnail = new qx.ui.basic.Image().set(
            {
                allowGrowX: false, allowGrowY: false,
                allowShrinkX: false, allowShrinkY: false,
                alignX: "center",
                alignY: "middle",
                maxHeight: 128, maxWidth: 128,
                scale: true
            });

        leftSide.add(thumbnail);
        thumbnail.exclude();

        vsp.add(leftSide, 1);
        vsp.add(rightSide, 3);
        this.add(vsp, {flex: 1});

        files.addListener("fileSelected", function (ev) {
            var spec = ev.getData();
            var ctype = spec ? spec["content_type"] : null;
            if (ncms.Utils.isImageContentType(ctype)) {
                thumbnail.setSource(ncms.Application.ACT.getRestUrl("media.thumbnail2", spec));
                thumbnail.show();
            } else {
                thumbnail.exclude();
            }
            var ff = this.getCtypeAcceptor() || function () {
                    return true;
                };
            this.__okBt.setEnabled((ctype != null && ff.call(ff, ctype)));
            if (spec) {
                this.setStatus(spec["folder"] + spec["name"]);
            } else {
                this.setStatus("");
            }
        }, this);


        if (opts["pageSpec"] && opts["pageSpec"].active == true) {
            files.setInpage(true);
        }
        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members: {

        __files: null,

        __okBt: null,

        close: function () {
            this.base(arguments);
            this.destroy();
        },

        __ok: function () {
            var ff = this.getCtypeAcceptor() || function () {
                    return true;
                };
            var sfiles = this.__files.getSelectedFiles();
            sfiles = sfiles.filter(
                function (spec) {
                    var ctype = spec ? spec["content_type"] : null;
                    return !!(ctype && ff(ctype));
                }
            );
            if (sfiles.length > 0) {
                this.fireDataEvent("completed", sfiles);
            }
        },

        __dispose: function () {
            this.__okBt = null;
            this.__files = null;
        }
    },

    destruct: function () {
        this.__dispose();
        //this._disposeObjects("__field_name");
    }
});
