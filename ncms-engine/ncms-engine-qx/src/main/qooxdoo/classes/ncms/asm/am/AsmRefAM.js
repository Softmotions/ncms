/**
 * Assembly reference attribute manager
 *
 * See java side AM: com.softmotions.ncms.asm.am.AsmRefAttributeManager.
 *
 * @asset(ncms/icon/16/misc/document-import.png)
 */
qx.Class.define("ncms.asm.am.AsmRefAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META:  {
            attributeTypes: "asmref",
            hidden: false,
            requiredSupported: false
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Include assembly");
        },

        getMetaInfo: function () {
            return ncms.asm.am.AsmRefAM.__META;
        }
    },

    members: {

        _form: null,

        _bf: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            var bf = this.__createBf(attrSpec, asmSpec);
            form.add(bf, this.tr("Assembly"), null, "assembly");
            this._form = form;
            return new sm.ui.form.FlexFormRenderer(form);
        },


        __createBf: function (attrSpec, asmSpec) {
            var bf = this._bf = new sm.ui.form.ButtonField(this.tr("Select assembly"),
                "ncms/icon/16/misc/document-import.png");
            bf.setPlaceholder(this.tr("Please select assembly to include"));
            bf.setReadOnly(true);
            bf.addListener("execute", function () {
                this.__onSelectAssembly(attrSpec, asmSpec);
            }, this);
            this._fetchAttributeValue(attrSpec, function (val) {
                if (val == null || val == "") {
                    bf.setValue(val);
                    return;
                }
                bf.setValue(val);
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("asms.basic", {"name": val}),
                    "GET", "application/json");
                req.send(function (resp) {
                    var data = resp.getContent();
                    var sb = [];
                    if (data["description"]) {
                        sb.push(data["description"]);
                        sb.push("|");
                    }
                    sb.push(data["name"]);
                    this._bf.setValue(sb.join(" "));
                    this._bf.setUserData("ref", data);
                }, this);
            }, this);
            bf.setRequired(true);
            return bf;
        },

        __onSelectAssembly: function (attrSpec, asmSpec) {
            var dlg = new ncms.asm.AsmSelectorDlg(
                this.tr("Please select assembly to include"), null,
                {type: "", exclude: asmSpec["id"], template: false},
                ["name", "description"]);
            dlg.open();
            dlg.addListener("completed", function (ev) {
                var data = ev.getData()[0];
                var sb = [];
                if (data["description"]) {
                    sb.push(data["description"]);
                    sb.push("|");
                }
                sb.push(data["name"]);
                this._bf.setValue(sb.join(" "));
                this._bf.setUserData("ref", data);
                dlg.close();
            }, this);
        },

        optionsAsJSON: function () {
            if (this._form == null || this._bf == null || !this._form.validate()) {
                return null;
            }
            var ref = this._bf.getUserData("ref");
            return {
                value: ref["id"]
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            return this.__createBf(attrSpec, asmSpec);
        },

        valueAsJSON: function () {
            if (this._bf == null) {
                return null;
            }
            var ref = this._bf.getUserData("ref");
            return {
                value: ref["id"]
            };
        }
    },

    destruct: function () {
        this._bf = null;
        this._disposeObjects("_form");
    }
});