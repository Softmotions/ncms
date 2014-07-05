/**
 * Files selector dialog dedicated to the specific page.
 */
qx.Class.define("ncms.mmgr.PageFilesSelectorDlg", {
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
         *   "owner" : "adam",
         *   "tags" : "foo, bar"
         *   }
         * or null
         */
        "completed" : "qx.event.type.Data"
    },

    properties : {

        /**
         * Accept content type filter function.
         * Function signature: (ctype {String}) => Boolean
         */
        ctypeAcceptor : {
            "check" : "Function",
            nullable : true
        }
    },

    /**
     *
     * @param pageId {Number} The page ID.
     * @param caption {String?} Dialog window caption.
     * @param options {Object?} ncms.mmgr.MediaFilesSelector options.
     */
    construct : function(pageId, caption, options) {
        qx.core.Assert.assertNumber(pageId, "Page ID is not a number");
        this.base(arguments, caption);
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal : true,
            showMinimize : false,
            showMaximize : true,
            allowMaximize : true,
            showStatusbar : true,
            width : 620,
            height : 450
        });

        var vsp = new qx.ui.splitpane.Pane("horizontal");
        var leftSide = new qx.ui.container.Composite(new qx.ui.layout.VBox());
        var files = this.__files = new ncms.mmgr.PageFilesSelector(pageId, options);
        leftSide.add(files, {flex : 1});

        //form
        var form = this.__form = new sm.ui.form.ExtendedForm();
        var linkTextTf = new qx.ui.form.TextField().set({maxLength : 64, required : true});
        form.add(linkTextTf, this.tr("Link text"), null, "linkText");
        leftSide.add(new sm.ui.form.FlexFormRenderer(form));

        var rightSide = new qx.ui.container.Composite(new qx.ui.layout.VBox());
        var thumbnail = new qx.ui.basic.Image().set(
                {allowGrowX : false, allowGrowY : false,
                    allowShrinkX : false, allowShrinkY : false,
                    alignX : "center",
                    alignY : "middle",
                    maxHeight : 128, maxWidth : 128,
                    scale : true});
        rightSide.add(thumbnail);
        thumbnail.exclude();

        vsp.add(leftSide, 1);
        vsp.add(rightSide, 0);
        this.add(vsp, {flex : 1});

        //Bottom buttons
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX" : "right"}));
        var bt = this.__okBt = new qx.ui.form.Button(this.tr("Ok")).set({enabled : false});
        bt.addListener("execute", this.__ok, this);
        hcont.add(bt);

        bt = new qx.ui.form.Button(this.tr("Cancel"));
        bt.addListener("execute", this.close, this);
        hcont.add(bt);
        this.add(hcont);

        files.addListener("fileSelected", function(ev) {
            var spec = ev.getData();
            var ctype = spec ? spec["content_type"] : null;
            if (ncms.Utils.isImageContentType(ctype)) {
                thumbnail.setSource(ncms.Application.ACT.getRestUrl("media.thumbnail2", spec));
                thumbnail.show();
            } else {
                thumbnail.exclude();
            }
            var ff = this.getCtypeAcceptor() || function() {
                return true;
            };
            this.__okBt.setEnabled((ctype != null && ff(ctype)));
            if (spec) {
                var ind = spec["name"].indexOf(".");
                linkTextTf.setValue(ind !== -1 ? spec["name"].substring(0, ind) : spec["name"]);
                this.setStatus(spec["folder"] + spec["name"]);
            } else {
                this.setStatus("");
            }
        }, this);

        var cmd = this.createCommand("Esc");
        cmd.addListener("execute", this.close, this);
        this.addListenerOnce("resize", this.center, this);
    },

    members : {

        __form : null,

        __files : null,

        __okBt : null,

        close : function() {
            this.base(arguments);
            this.destroy();
        },

        __ok : function() {
            var spec = this.__files.getSelectedFile();
            var ctype = spec ? spec["content_type"] : null;
            var ff = this.getCtypeAcceptor() || function() {
                return true;
            };
            if (ctype && ff(ctype)) {
                this.__form.populateJSONObject(spec);
                this.fireDataEvent("completed", spec);
            }
        }
    },

    destruct : function() {
        this.__okBt = null;
        this.__files = null;
        this._disposeObjects("__form");
    }
});
