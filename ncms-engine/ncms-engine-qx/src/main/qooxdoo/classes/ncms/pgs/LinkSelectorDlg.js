/**
 * Internal/external links selector dialog.
 */
qx.Class.define("ncms.pgs.LinkSelectorDlg", {
    extend : ncms.pgs.PagesSelectorDlg,

    construct : function(caption, options) {
        this.__options = options || {};
        this.base(arguments, caption, false);

    },

    members : {

        __options : null,

        __form : null,

        __outLinkTf : null,

        _initForm : function() {
            var options = this.__options;
            var form = this.__form = new sm.ui.form.ExtendedForm();
            if (options["includeLinkName"] == null || options["includeLinkName"] == true) {
                var lname = new qx.ui.form.TextField().set({required : options["requireLinkName"] === undefined ? true : !!options["requireLinkName"]});
                if (options["linkText"] != null && options["linkText"] != "") {
                    lname.setValue(options["linkText"]);
                }
                form.add(lname, this.tr("Link text"), null, "linkText");
                this._selector.addListener("pageSelected", function(ev) {
                    var ps = ev.getData();
                    if (ps == null) {
                        lname.setValue("");
                    } else {
                        lname.setValue(ps["name"]);
                    }
                });
            }

            if (options["allowExternalLinks"] === true) {
                var outLink = this.__outLinkTf = new qx.ui.form.TextField();
                outLink.setPlaceholder("http://");
                form.add(outLink, this.tr("Or set external link"), null, "externalLink");
                outLink.addListener("input", this._syncState, this);
            }
            this.add(new sm.ui.form.OneColumnFormRenderer(form));
        },

        _syncState : function() {
            this.base(arguments);
            if (this.__outLinkTf != null && this._okBt.getEnabled() == false) {
                this._okBt.setEnabled(!sm.lang.String.isEmpty(this.__outLinkTf.getValue()));
            }
        },

        _ok : function() {
            if (!this.__form.validate()) {
                return;
            }
            this._selector.getSelectedPageWithExtraInfo(function(sp) {
                sp = sm.lang.Object.shallowClone(sp || {});
                this.__form.populateJSONObject(sp);
                if (sp["id"] == null && sp["externalLink"]) {
                    sp["id"] = sp["externalLink"];
                }
                this.fireDataEvent("completed", sp);
            }, this);
        }
    },

    destruct : function() {
        this.__options = null;
        this.__outLinkTf = null;
        this._disposeObjects("__form");
    }
});