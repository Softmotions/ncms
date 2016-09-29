/**
 * Page reference.
 * @asset (ncms/icon/16/misc/chain-plus.png)
 */
qx.Class.define("ncms.asm.am.PageRefAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: ["pageref"],
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Page reference");
        },

        getMetaInfo: function () {
            return ncms.asm.am.PageRefAM.__META;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var bf = this.__initPageSelectorBf(attrSpec, true);
            this._fetchAttributeValue(attrSpec, function (val) {
                bf.setValue(val);
            });
            form.add(bf, this.tr("Link"), null, "link");

            var webCb = new qx.ui.form.CheckBox();
            webCb.setValue("true" == opts["allowExternalLinks"]);
            form.add(webCb, this.tr("Allow external links"), null, "allowExternalLinks");

            var fr = new sm.ui.form.FlexFormRenderer(form);
            this._form = form;
            return fr;
        },

        optionsAsJSON: function () {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var items = this._form.getItems();
            return {
                value: items["link"].getValue(),
                allowExternalLinks: items["allowExternalLinks"].getValue()
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var bf = this.__initPageSelectorBf(attrSpec);
            this._fetchAttributeValue(attrSpec, function (val) {
                bf.setValue(val);
            });
            bf.setUserData("ncms.asm.validator", this.__validatePageSelector);
            this._valueWidget = bf;
            return bf;
        },

        valueAsJSON: function () {
            if (this._valueWidget == null) {
                return;
            }
            return {
                value: this._valueWidget.getValue()
            }
        },

        __initPageSelectorBf: function (attrSpec, inOpts) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var bf = new sm.ui.form.ButtonField(this.tr("Page"), "ncms/icon/16/misc/chain-plus.png");
            bf.setShowResetButton(true);
            bf.setReadOnly(true);
            if (!inOpts) {
                bf.setRequired(!!attrSpec["required"]);
            }
            bf.setPlaceholder(this.tr("Please set a page link"));
            bf.addListener("reset", function () {
                bf.resetValue();
            });
            bf.addListener("execute", function () {
                var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Please set a page link"), {
                    allowExternalLinks: opts["allowExternalLinks"] == "true"
                });
                dlg.addListener("completed", function (ev) {
                    var data = ev.getData();
                    //{"id":1,"name":"Лендинги","accessMask":"ownd","idPath":[1],"labelPath":["Лендинги"],
                    // "guidPath":["d76200ca883563e4d971e38951b332c0"],"linkText":"Лендинги","externalLink":null}

                    //{"id":1,"name":"Лендинги","accessMask":"ownd","idPath":[1],"labelPath":["Лендинги"],
                    //"guidPath":["d76200ca883563e4d971e38951b332c0"],"linkText":"Лендинги",
                    //"externalLink":"http://www.yandex.ru"}
                    var val = [];
                    if (!sm.lang.String.isEmpty(data["externalLink"])) {
                        val.push(data["externalLink"]);
                    } else {
                        val.push("page:" + sm.lang.Array.lastElement(data["guidPath"]));
                    }
                    if (!sm.lang.String.isEmpty(data["linkText"])) {
                        val.push(data["linkText"]);
                    }
                    bf.setValue(val.join(" | "));
                    dlg.close();
                }, this);
                dlg.open();
            }, this);
            return bf;
        },

        __validatePageSelector: function (value, bf) {
            if (bf.getRequired() && sm.lang.String.isEmpty(bf.getValue())) {
                bf.setInvalidMessage(this.tr("This field is required"));
                bf.setValid(false);
                return false;
            } else {
                bf.setValid(true);
                return true;
            }
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }

});
