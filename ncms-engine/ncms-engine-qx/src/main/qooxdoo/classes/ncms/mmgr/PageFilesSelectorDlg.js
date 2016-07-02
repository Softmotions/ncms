/**
 * Files selector dialog dedicated to the specific page.
 */
qx.Class.define("ncms.mmgr.PageFilesSelectorDlg", {
    extend: qx.ui.window.Window,

    events: {

        /**
         * Data example:
         *
         * {"id":2,
         *   "name":"496694.png",
         *   "folder":"/test/",
         *   "content_type":"image/png",
         *   "content_length":10736,
         *   "owner" : "adam",
         *   "tags" : "foo, bar"
         *   }
         * or null
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
     * options:
     * <code>
     *  {
     *      linkText : {Boolean?true}
     *      syncLinkText : {Boolean?true} //Do not allow link text filed <-> file name sync
     *      allowModify : {Boolean?false},
     *      allowMove : {Boolean?false},
     *      allowSubfoldersView : {Boolean?false},
     *      smode : qx.ui.table.selection.Model.(SINGLE_SELECTION | SINGLE_INTERVAL_SELECTION | MULTIPLE_INTERVAL_SELECTION | MULTIPLE_INTERVAL_SELECTION_TOGGLE)
     *      noActions : {Boolean?false} //If true dialog will have only 'Ok' control simple closing this dialog
     *
     *  }
     * </code>
     * @param pageId {Number} The page ID.
     * @param caption {String?} Dialog window caption.
     * @param options {Object?} ncms.mmgr.MediaFilesSelector options.
     */
    construct: function (pageId, caption, options) {
        options = options || {};
        if (options["smode"] == null) {
            options["smode"] = qx.ui.table.selection.Model.SINGLE_SELECTION;
        }
        qx.core.Assert.assertNumber(pageId, "Page ID is not a number");
        this.base(arguments, caption);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            showStatusbar: true,
            width: 620,
            height: 450
        });

        var vsp = new qx.ui.splitpane.Pane("horizontal");
        var leftSide = new qx.ui.container.Composite(new qx.ui.layout.VBox(5));
        var files = this._files = new ncms.mmgr.PageFilesSelector(pageId, options);
        files.getTable().setStatusBarVisible(false);
        leftSide.add(files, {flex: 1});

        //form
        var form = this._form = new sm.ui.form.ExtendedForm();
        var linkTextTf = null;
        if (options["linkText"] !== false) {
            linkTextTf = new qx.ui.form.TextField().set({maxLength: 128});
            form.add(linkTextTf, this.tr("Link text"), null, "linkText");
        }
        this._initForm(form);
        var fr = this._createFormRenderer(form);
        fr.setPaddingRight(5);
        leftSide.add(fr);

        var rightSide = new qx.ui.container.Composite(new qx.ui.layout.VBox());
        var thumbnail = new qx.ui.basic.Image().set(
            {
                allowGrowX: false, allowGrowY: false,
                allowShrinkX: false, allowShrinkY: false,
                alignX: "center",
                alignY: "middle",
                maxHeight: 128, maxWidth: 128,
                scale: true
            });
        rightSide.add(thumbnail);
        thumbnail.exclude();

        vsp.add(leftSide, 1);
        vsp.add(rightSide, 0);
        this.add(vsp, {flex: 1});

        //Bottom buttons
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));

        if (options["noActions"] === true) {
            var bt = new qx.ui.form.Button(this.tr("Close"));
            bt.addListener("execute", this.close, this);
            hcont.add(bt);
        } else {
            var bt = this._okBt = new qx.ui.form.Button(this.tr("Ok")).set({enabled: false});
            bt.addListener("execute", this._ok, this);
            hcont.add(bt);

            bt = new qx.ui.form.Button(this.tr("Cancel"));
            bt.addListener("execute", this.close, this);
            hcont.add(bt);
        }
        this.add(hcont);

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
            if (this._okBt) {
                this._okBt.setEnabled((ctype != null && ff(ctype)));
            }
            if (spec) {
                var ind = spec["name"].indexOf(".");
                if (linkTextTf && options["syncLinkText"] !== false) {
                    linkTextTf.setValue(ind !== -1 ? spec["name"].substring(0, ind) : spec["name"]);
                }
                this.setStatus(spec["folder"] + spec["name"]);
            } else {
                this.setStatus("");
            }
        }, this);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members: {

        _form: null,

        _files: null,

        _okBt: null,

        close: function () {
            this.base(arguments);
            this.destroy();
        },

        _initForm: function (form) {

        },

        _createFormRenderer: function (form) {
            return new sm.ui.form.FlexFormRenderer(form);
        },

        getSelectedFiles: function () {
            var cta = this.getCtypeAcceptor() || function () {
                    return true;
                };
            return this._files.getSelectedFiles().filter(function (f) {
                return cta(f["content_type"]);
            }, this);
        },

        getSelectedFile: function () {
            return this.getSelectedFiles()[0];
        },

        _ok: function () {
            var f = this.getSelectedFile();
            if (f == null) {
                return;
            }
            this._form.populateJSONObject(f);
            this.fireDataEvent("completed", f);
        }
    },

    destruct: function () {
        this._okBt = null;
        this._files = null;
        this._disposeObjects("_form");
    }
});
