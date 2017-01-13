/**
 * Insert image dialog
 *
 * @asset(ncms/icon/16/wiki/link_add.png)
 */
qx.Class.define("ncms.wiki.InsertImageDlg", {
    extend: ncms.mmgr.PageFilesSelectorDlg,

    construct: function (pageId, caption, opts) {
        this.__opts = opts || {};
        this.base(arguments, pageId, caption, {linkText: false, allowModify: true});
        this.setCtypeAcceptor(ncms.Utils.isImageContentType.bind(ncms.Utils));
        this.set({
            width: 620,
            height: 550
        });

    },

    members: {

        __opts: null,

        _initForm: function (form) {
            var caption = new qx.ui.form.TextField().set({maxLength: 256});
            form.add(caption, this.tr("Caption"), null, "caption");

            if (!this.__opts["noSize"]) {
                var rg = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
                rg.add(new qx.ui.form.RadioButton(this.tr("original")).set({"model": "original"}));
                rg.add(new qx.ui.form.RadioButton(this.tr("small")).set({"model": "small"}));
                rg.add(new qx.ui.form.RadioButton(this.tr("medium")).set({"model": "medium"}));
                rg.add(new qx.ui.form.RadioButton(this.tr("large")).set({"model": "large"}));
                form.add(rg, this.tr("Size"), null, "size");
            }

            if (!this.__opts["noPosition"]) {
                rg = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
                rg.add(new qx.ui.form.RadioButton(this.tr("default")).set({"model": "none"}));
                rg.add(new qx.ui.form.RadioButton(this.tr("center")).set({"model": "center"}));
                rg.add(new qx.ui.form.RadioButton(this.tr("left")).set({"model": "left"}));
                rg.add(new qx.ui.form.RadioButton(this.tr("right")).set({"model": "right"}));
                form.add(rg, this.tr("Position"), null, "position");
            }

            if (!this.__opts["noLink"]) {
                var bf = new sm.ui.form.ButtonField(null, "ncms/icon/16/wiki/link_add.png", true);
                bf.setShowResetButton(true);
                bf.setReadOnly(true);
                bf.addListener("reset", bf.resetValue, bf);
                bf.addListener("execute", function () {
                    var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Select image link"), {
                        includeLinkName: false,
                        allowExternalLinks: true
                    });
                    dlg.open();
                    dlg.addListener("completed", function (ev) {
                        var data = ev.getData();
                        if (!sm.lang.String.isEmpty(data["externalLink"])) {
                            bf.setValue(data["externalLink"]);
                        } else if (Array.isArray(data["guidPath"]) && data["guidPath"].length > 0) {
                            var guid = data["guidPath"][data["guidPath"].length - 1];
                            var val = ["Page:" + guid];
                            bf.setValue(val.join(""));
                        }
                        dlg.close();
                    });
                }, this);
                form.add(bf, this.tr("Link"), null, "link");
            }
        },

        _createFormRenderer: function (form) {
            return new sm.ui.form.FlexFormRenderer(form);
        }

    },

    destruct: function () {
        this.__opts = null;
        //this._disposeObjects("__field_name");                                
    }
});