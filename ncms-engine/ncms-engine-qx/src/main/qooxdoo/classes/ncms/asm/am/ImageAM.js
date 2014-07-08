/**
 * Image attribute manager
 *
 * @asset(ncms/icon/16/misc/image.png)
 */
qx.Class.define("ncms.asm.am.ImageAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Image");
        },

        getSupportedAttributeTypes : function() {
            return [ "image" ];
        }
    },

    members : {

        _form : null,

        _bf : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var form = this._form = new sm.ui.form.ExtendedForm();
            var el = new qx.ui.form.TextField().set({maxLength : 3});
            if (opts["width"] != null) {
                el.setValue(opts["width"]);
            }
            form.add(el, this.tr("Width"), null, "width");

            el = new qx.ui.form.TextField().set({maxLength : 3});
            if (opts["height"] != null) {
                el.setValue(opts["height"]);
            }
            form.add(el, this.tr("Height"), null, "height");

            var autoCb = new qx.ui.form.CheckBox();
            autoCb.setValue(opts["resize"] == "true");
            form.add(autoCb, this.tr("Auto resize"), null, "resize");
            autoCb.addListener("changeValue", function(ev) {
                var val = ev.getData();
                if (val === true && restrictCb.getValue()) {
                    restrictCb.setValue(false);
                }
            });

            var restrictCb = new qx.ui.form.CheckBox();
            restrictCb.setValue(opts["restrict"] == "true");
            form.add(restrictCb, this.tr("Restrict sizes"), null, "restrict");
            restrictCb.addListener("changeValue", function(ev) {
                var val = ev.getData();
                if (val === true && autoCb.getValue()) {
                    autoCb.setValue(false);
                }
            });

            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        optionsAsJSON : function() {
            if (this._form == null || !this._form.validate()) {
                return null;
            }
            return this._form.populateJSONObject({});
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var w = new qx.ui.container.Composite(new qx.ui.layout.VBox(5));

            var bf = this._bf = new sm.ui.form.ButtonField(this.tr("Select image"), "ncms/icon/16/misc/image.png", true);
            bf.setReadOnly(true);
            bf.addState("widgetNotReady");
            //bf.setEnabled(false);
            bf.addListener("execute", function() {
                var dlg = new ncms.mmgr.PageFilesSelectorDlg(
                        asmSpec["id"],
                        this.tr("Select image file"), {
                            allowModify : true
                        });
                dlg.setCtypeAcceptor(ncms.Utils.isImageContentType);
                dlg.open();
                dlg.addListener("completed", function(ev) {
                    var data = ev.getData();
                    var udata = {
                        id : data["id"],
                        options : opts
                    };
                    bf.setUserData("value", udata);
                    bf.setValue(data["folder"] + data["name"]);
                    image.setSource(ncms.Application.ACT.getRestUrl("media.thumbnail2", udata));
                    dlg.close();
                });
            }, this);
            w.add(bf);

            var image = new qx.ui.basic.Image();
            w.add(image);

            this._fetchAttributeValue(attrSpec, function(val) {
                if (sm.lang.String.isEmpty(val)) {
                    bf.resetValue();
                    bf.removeState("widgetNotReady");
                    image.resetSource();
                    return;
                }
                val = JSON.parse(val);
                bf.setUserData("value", val);
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.path", val), "GET");
                req.send(function(resp) {
                    bf.setValue(resp.getContent());
                }, this);
                req.addListener("finished", function() {
                    bf.removeState("widgetNotReady");
                });
                image.setSource(ncms.Application.ACT.getRestUrl("media.thumbnail2", val));
            }, this);

            w.setUserData("ncms.asm.activeWidget", bf);
            this._valueWidget = w;
            return w;
        },

        valueAsJSON : function() {
            return this._bf.getUserData("value");
        }
    },

    destruct : function() {
        this._bf = null;
        this._disposeObjects("_form");
    }
});
