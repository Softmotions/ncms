/**
 * Pages selector dialog.
 */
qx.Class.define("ncms.pgs.PagesSelectorDlg", {
    extend: qx.ui.window.Window,

    events: {

        /**
         * Data: {
         *   id : {Number} Page ID,
         *   name : {String} Page name,
         *   accessMask : {String} Page access mask
         * }
         */
        "completed": "qx.event.type.Data"
    },

    /**
     *
     * @param caption {String?} Dialog caption
     * @param allowModify {Boolean?false} Allow CRUD operations on pages
     * @param options {Map?} Options:
     *                <code>
     *                    {
     *                      foldersOnly : {Boolean?false} //Show only folders,
     *                      allowRootSelection : {Boolen?false},
     *                      accessAll : {String?} //Optional access all page security restriction
     *                    }
     *                </code>
     *
     */
    construct: function (caption, allowModify, options) {
        this._options = options || {};
        this.base(arguments, caption != null ? caption : this.tr("Select the page"));
        this.setLayout(new qx.ui.layout.VBox(5));
        this.set({
            modal: true,
            showMinimize: false,
            showMaximize: true,
            allowMaximize: true,
            width: 620,
            height: 500
        });

        var selector = this._selector = new ncms.pgs.PagesSelector(!!allowModify, options);
        this.add(selector, {flex: 1});

        this._initForm();

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5).set({"alignX": "right"}));
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

        selector.addListener("selected", this._syncState, this);
        this._syncState();

        selector.getTreeSelector().addListenerOnce("treeLoaded", function () {
            var tree = selector.getTreeSelector().getTree();
            var pane = tree.getPane();
            pane.addListener("cellDbltap", function (ev) {
                var row = ev.getRow();
                var item = tree.getLookupTable().getItem(row);
                if (tree.isNode(item)) {
                    return;
                }
                this._ok();
            }, this);
            tree.focus();
        }, this);
    },

    members: {

        _options: null,

        _selector: null,

        _okBt: null,

        _ok: function () {
            var asm = this._selector.getSelectedAsm();
            if (asm != null) {
                if (this._options["pageId"] != asm["id"]) {
                    this.fireDataEvent("completed", qx.lang.Object.mergeWith({}, asm))
                }
            } else {
                var sp = this._selector.getSelectedPage();
                if (sp && sp["id"] == this._options["pageId"]) {
                    return;
                }
                this._selector.getSelectedPageWithExtraInfo(function (sp) {
                    if (sp != null || this._options["allowRootSelection"]) {
                        this.fireDataEvent("completed", sp);
                    }
                }, this);
            }
        },

        _syncState: function () {
            var asm = this._selector.getSelectedAsm();
            var sp = this._selector.getSelectedPage();
            if (asm != null) {
                var same = this._options["pageId"] == asm["id"];
                this._okBt.setEnabled(!same);
                if (!same && asm["dblClick"]) {
                    this._ok();
                }
            } else {
                if (sp != null &&
                    this._options["pageId"] == sp["id"]) {
                    sp = null;
                }
                if (!this._options["allowRootSelection"]) {
                    this._okBt.setEnabled(sp != null);
                }
                if (sp != null) {
                    if (this._options["accessAll"]) {
                        this._okBt.setEnabled(ncms.Utils.checkAccessAll(sp["accessMask"], this._options["accessAll"]));
                    }
                }
            }
        },

        _initForm: function () {

        },

        close: function () {
            this.base(arguments);
            this.destroy();
        }
    },

    destruct: function () {
        this._okBt = null;
        this._selector = null;
        this._options = null;
    }
});
