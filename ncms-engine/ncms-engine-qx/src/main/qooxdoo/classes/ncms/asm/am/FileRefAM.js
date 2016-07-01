/**
 * File reference
 *
 * @asset(ncms/icon/16/misc/document-text.png)
 */
qx.Class.define("ncms.asm.am.FileRefAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Include file resource");
        },

        getSupportedAttributeTypes : function() {
            return [ "fileref" ];
        },

        isHidden : function() {
            return false;
        }
    },

    members : {

        _form : null,

        /**
         * attrSpec example:
         *
         * {
         *   "asmId" : 1,
         *   "name" : "copyright",
         *   "type" : "string",
         *   "value" : "My company (c)",
         *   "options" : "foo=bar, foo2=bar2",
         *   "hasLargeValue" : false
         * },
         */
        activateOptionsWidget : function(attrSpec, asmSpec) {
            var form = new qx.ui.form.Form();
            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);

            //Button field
            var bf = this._initFileSelectorBf(attrSpec, true);
            this._fetchAttributeValue(attrSpec, function(val) {
                bf.setValue(val);
            });
            form.add(bf, this.tr("Location"), null, "location");

            var astCb = new qx.ui.form.CheckBox();
            astCb.setValue("true" == opts["asTemplate"]);
            form.add(astCb, this.tr("Render as template"), null, "asTemplate");

            var aslocCb = new qx.ui.form.CheckBox();
            aslocCb.setValue("true" == opts["asLocation"]);
            aslocCb.addListener("changeValue", function(ev) {
                //escCb.setEnabled(ev.getData() == false);
                astCb.setEnabled(ev.getData() == false);
            });
            form.add(aslocCb, this.tr("Render only location"), null, "asLocation");
            //escCb.setEnabled(aslocCb.getValue() == false);
            astCb.setEnabled(aslocCb.getValue() == false);

            var fr = new sm.ui.form.FlexFormRenderer(form);
            this._form = form;
            return fr;
        },

        optionsAsJSON : function() {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            var items = this._form.getItems();
            return {
                asTemplate : items["asTemplate"].getValue(),
                asLocation : items["asLocation"].getValue(),
                //escape : items["escape"].getValue(),
                value : items["location"].getValue()
            };
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var bf = this._initFileSelectorBf(attrSpec);
            this._fetchAttributeValue(attrSpec, function(val) {
                bf.setValue(val);
            });
            bf.setUserData("ncms.asm.validator", this.__validateFileSelector);
            this._valueWidget = bf;
            return bf;
        },

        _initFileSelectorBf: function (attrSpec, inOpts) {
            var bf = new sm.ui.form.ButtonField(this.tr("File"),
                    "ncms/icon/16/misc/document-text.png");
            bf.setShowResetButton(true);
            bf.setReadOnly(true);
            if (!inOpts) {
                bf.setRequired(!!attrSpec["required"]);
            }
            bf.setPlaceholder(this.tr("Please specify a file"));
            bf.addListener("reset", function() {
                bf.resetValue();
            });
            bf.addListener("execute", function() {
                var dlg = new ncms.mmgr.MediaSelectFileDlg(
                        true,
                        this.tr("Please specify a file"));
                dlg.setCtypeAcceptor(ncms.Utils.isTextualContentType.bind(ncms.Utils));
                dlg.addListener("completed", function(ev) {
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
            return bf;
        },

        __validateFileSelector : function(value, bf) {
            if (bf.getRequired() && sm.lang.String.isEmpty(bf.getValue())) {
                bf.setInvalidMessage(this.tr("This field is required"));
                bf.setValid(false);
                return false;
            } else {
                bf.setValid(true);
                return true;
            }
        },

        valueAsJSON : function() {
            if (this._valueWidget == null) {
                return;
            }
            return {
                value : this._valueWidget.getValue()
            }
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});