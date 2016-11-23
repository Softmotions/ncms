/**
 * Textual file editor dialog.
 */
qx.Class.define("ncms.mmgr.MediaTextFileEditorDlg", {

    extend: qx.ui.window.Window,


    statics: {

        __EDITOR: null
    },

    /**
     * @param fileSpec {Object} File specification. See `ncms.mmgr.MediaTextFileEditor#fileSpec`
     * @param opts {Object?}
     *              - caption {String?} Window caption
     *              - width   {Number?} Window width
     *              - height  {Number?} Window height
     */
    construct: function (fileSpec, opts) {
        this.assertMap(fileSpec);
        opts = opts || {};
        var caption = opts["caption"] || this.tr("Edit file %1", fileSpec["name"]);
        var root = qx.core.Init.getApplication().getRoot();
        var bounds = root.getBounds();
        if (bounds) {
            if (!opts["height"]) {
                opts["height"] = Math.max(300, parseInt(bounds.height * 0.8));
            }
        }

        this.base(arguments, caption);
        this.setLayout(new qx.ui.layout.Grow());
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            showStatusbar: false,
            width: opts["width"] || 700,
            height: opts["height"] || 450
        });

        this.__editor = ncms.mmgr.MediaTextFileEditorDlg.__EDITOR;
        if (this.__editor == null) {
            this.__editor = ncms.mmgr.MediaTextFileEditorDlg.__EDITOR = new ncms.mmgr.MediaTextFileEditor({ui: "dlgEditor"});
        }
        this.__editor.setFileSpec(fileSpec);
        this.add(this.__editor);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members: {

        __editor: null,

        close: function () {
            this.base(arguments);
            if (this.__editor) {
                this.__editor.setFileSpec(null);
            }
        },

        __dispose: function () {
            this.__editor = null;
        }
    },

    destruct: function () {
        this.__dispose();
    }
});