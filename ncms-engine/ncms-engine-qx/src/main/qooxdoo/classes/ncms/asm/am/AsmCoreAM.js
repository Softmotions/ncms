/**
 * Asm core widget.
 *
 * @asset(ncms/icon/16/actions/core_link.png)
 * @asset(ncms/icon/16/actions/edit-document.png)
 */
qx.Class.define("ncms.asm.am.AsmCoreAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "core",
            hidden: false,
            requiredSupported: false
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Assembly core selector");
        },

        getMetaInfo: function () {
            return ncms.asm.am.AsmCoreAM.__META;
        }
    },

    members: {

        activateOptionsWidget: function (attrSpec, asmSpec) {
            return null;
        },

        optionsAsJSON: function () {
            return {};
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var bf = this._initCoreSelectorBf(true, asmSpec);
            bf.setValue(this._fetchAsmCoreLocation(asmSpec));
            bf.setUserData("ncms.asm.validator", this.__validateFileSelector);
            this._valueWidget = bf;
            return bf;
        },

        _fetchAsmCoreLocation: function (asmSpec) {
            var core = asmSpec["core"];
            if (core == null) {
                return "";
            }
            return core["location"] || "";
        },

        _initCoreSelectorBf: function (label, asmSpec) {
            var bf = new sm.ui.form.ButtonField(label ? this.tr("File") : null,
                "ncms/icon/16/actions/core_link.png", false, null,
                {
                    "extraButtonIcon": "ncms/icon/16/actions/edit-document.png"
                });
            bf.setShowResetButton(true);
            bf.setShowExtraButton(true);
            bf.setReadOnly(true);
            bf.setRequired(true);
            bf.setPlaceholder(this.tr("Please specify a core location"));
            bf.addListener("reset", function () {
                bf.resetValue();
            });
            bf.addListener("execute", function () {
                var dlg = new ncms.mmgr.MediaSelectFileDlg(
                    true,
                    this.tr("Please specify the core location for: '%1'", asmSpec["name"]), {
                        pageSpec: {id: asmSpec["id"], name: asmSpec["name"], active: true}
                    });
                dlg.setCtypeAcceptor(ncms.Utils.isTextualContentType.bind(ncms.Utils));
                dlg.addListener("completed", function (ev) {
                    var fspec = ev.getData()[0];
                    //fspec={"id":115,"name":"head.httl",
                    // "folder":"/site/cores/inc/",
                    // "content_type":"text/plain; charset=UTF-8",
                    // "owner":"system",
                    // "content_length":4,
                    // "description":null,
                    // "tags":null}
                    var path = fspec["folder"] + fspec["name"];
                    bf.setValue(path);
                    dlg.close();
                    if (this._form != null) {
                        this._form.validate();
                    } else {
                        this.__validateFileSelector(path, bf);
                    }
                }, this);
                dlg.show();
            }, this);
            bf.addListener("extra", function () {
                var location = bf.getTextField().getValue();
                if (ncms.Utils.isTextualFilePath(location)) {
                    ncms.mmgr.MediaFilesUtils.fetchMediaInfo(location, function (meta) {
                        var dlg = new ncms.mmgr.MediaTextFileEditorDlg(meta);
                        dlg.open();
                    }, this);
                }
            }, this);
            bf.getTextField().bind("value", bf.getExtraButton(), "enabled", {
                converter: function (v) {
                    return bf.getEnabled() && ncms.Utils.isTextualFilePath(v);
                }
            });
            return bf;
        },

        __validateFileSelector: function (value, bf) {
            if (sm.lang.String.isEmpty(bf.getValue())) {
                bf.setInvalidMessage(this.tr("This field is required"));
                bf.setValid(false);
                return false;
            } else {
                bf.setValid(true);
                return true;
            }
        },

        valueAsJSON: function () {
            if (this._valueWidget == null) {
                return;
            }
            return {
                value: this._valueWidget.getValue()
            }
        }
    },

    destruct: function () {
    }
});