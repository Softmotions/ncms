/**
 * RichRefAM value editor.
 * @asset (ncms/icon/16/misc/chain-plus.png)
 */
qx.Class.define("ncms.asm.am.RichRefAMValueWidget", {
    extend: qx.ui.core.Widget,
    implement: [qx.ui.form.IModel,
        ncms.asm.am.IValueWidget],
    include: [ncms.asm.am.MValueWidget],

    properties: {
        model: {
            check: "Object",
            nullable: true,
            event: "changeModel",
            apply: "__applyModel"
        }
    },

    construct: function (attrSpec, asmSpec) {
        this.__asmSpec = asmSpec;
        this.__attrSpec = attrSpec;

        this.base(arguments);

        this._setLayout(new qx.ui.layout.Grow());
        this.addState("widgetNotReady");

        var opts = ncms.Utils.parseOptions(attrSpec["options"]);
        var el;
        var form = this.__form = new qx.ui.form.Form();
        var menuspec = [];
        if (opts["allowPages"] == null || opts["allowPages"] === "true") {
            menuspec.push([this.tr("Page link").toString(), "page"]);
        }
        if (opts["allowFiles"] === "true") {
            menuspec.push([this.tr("File link").toString(), "file"]);
        }
        if (opts["allowName"] === "true") {
            el = new qx.ui.form.TextField();
            el.setRequired(true);
            el.setMaxLength(512);
            el.addListener("input", this.__modified, this);
            form.add(el, this.tr("Name"), null, "customName");
        }
        if (menuspec.length > 0) {
            var bf = this.__bf = new sm.ui.form.ButtonField(null, "ncms/icon/16/misc/chain-plus.png", true, menuspec);
            if (opts["optionalLinks"] === "true") {
                bf.setShowResetButton(true);
                bf.setRequired(false);
                bf.addListener("reset", function () {
                    bf.resetValue();
                });
            } else {
                bf.setRequired(true);
            }
            bf.setReadOnly(true);
            bf.addListener("execute", this.__onSetLink, this);
            bf.addListener("changeValue", this.__modified, this);
            form.add(bf, this.tr("Link"), null, "link");
        }

        if (opts["allowImage"] === "true") {
            var iAttrSpec = sm.lang.Object.shallowClone(attrSpec);
            iAttrSpec["hasLargeValue"] = false;
            iAttrSpec["value"] = "null";
            iAttrSpec["options"] = opts["image"];
            iAttrSpec["required"] = true;
            this.__imageAM = new ncms.asm.am.ImageAM();
            var iw = this.__imageAM.activateValueEditorWidget(iAttrSpec, asmSpec);
            var validator = null;
            if (typeof iw.getUserData("ncms.asm.validator") === "function") {
                validator = iw.getUserData("ncms.asm.validator");
            } else if (typeof iw.getValidator === "function") {
                validator = iw.getValidator();
            }
            iw.addListener("modified", this.__modified, this);
            form.add(iw, this.tr("Image"), validator, "image", iw);
        }

        var styles = ncms.Utils.parseOptions(opts["styles"]);
        if (Object.keys(styles).length > 0) { //we have a styles
            el = new qx.ui.form.SelectBox();
            Object.keys(styles).forEach(function (k) {
                el.add(new qx.ui.form.ListItem(k, null, styles[k]));
            });
            form.add(el, this.tr("Option"), null, "styles");
        }

        styles = ncms.Utils.parseOptions(opts["styles2"]);
        if (Object.keys(styles).length > 0) { //we have a styles
            el = new qx.ui.form.SelectBox();
            Object.keys(styles).forEach(function (k) {
                el.add(new qx.ui.form.ListItem(k, null, styles[k]));
            });
            form.add(el, this.tr("Option"), null, "styles2");
        }

        styles = ncms.Utils.parseOptions(opts["styles3"]);
        if (Object.keys(styles).length > 0) { //we have a styles
            el = new qx.ui.form.SelectBox();
            Object.keys(styles).forEach(function (k) {
                el.add(new qx.ui.form.ListItem(k, null, styles[k]));
            });
            form.add(el, this.tr("Option"), null, "styles3");
        }

        if (opts["allowDescription"] === "true") {
            el = new qx.ui.form.TextArea();
            el.setAutoSize(true);
            el.setRequired(false);
            el.setMaxLength(4096);
            el.addListener("input", this.__modified, this);
            form.add(el, this.tr("Extra"), null, "description");
        }
        var fr = new sm.ui.form.FlexFormRenderer(form);
        if (opts["allowDescription"] === "true") { // expand only description
            fr.setLastRowFlexible();
        }
        this._add(fr);
    },

    members: {

        __bf: null,

        __imageAM: null,

        __form: null,

        __asmSpec: null,

        __attrSpec: null,

        __modified: function () {
            if (this.hasState("widgetNotReady")) {
                return;
            }
            this.fireEvent("modified");
        },

        __onSetLink: function (ev) {
            var dlg;
            var data = ev.getData();
            if (data == null || data === "page") {
                dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Please set a page link"), {
                    allowExternalLinks: true
                });
                dlg.addListener("completed", function (ev) {
                    var data = ev.getData();
                    var val = [];
                    if (!sm.lang.String.isEmpty(data["externalLink"])) {
                        val.push(data["externalLink"]);
                    } else {
                        val.push("page:" + sm.lang.Array.lastElement(data["guidPath"]));
                    }
                    if (!sm.lang.String.isEmpty(data["linkText"])) {
                        val.push(data["linkText"]);
                    }
                    this.__bf.setValue(val.join(" | "));
                    dlg.close();
                }, this);
                dlg.open();
            } else {
                dlg = new ncms.mmgr.PageFilesSelectorDlg(
                    this.__asmSpec["id"],
                    this.tr("Please set the file link"),
                    {
                        allowModify: true,
                        allowSubfoldersView: true,
                        smode: qx.ui.table.selection.Model.SINGLE_SELECTION
                    }
                );
                dlg.addListener("completed", function (ev) {
                    var data = ev.getData();
                    //{"id":1221,"name":"зеленый цветок.jpg",
                    //"folder":"/pages/963/","content_type":"image/jpeg",
                    //"owner":"admin","owner_fullName":"Антон Адаманский","content_length":35195,"description":null,"tags":null,"linkText":"зеленый цветок"}
                    var val = [];
                    val.push("media:/" + data["id"] + "/" + data["name"]);
                    if (!sm.lang.String.isEmpty(data["linkText"])) {
                        val.push(data["linkText"]);
                    }
                    this.__bf.setValue(val.join(" | "));
                    dlg.close();
                }, this);
                dlg.open();
            }
        },

        __applyModel: function (model) {
            model = model || {};
            //qx.log.Logger.info("Apply model " + JSON.stringify(model));
            this.addState("widgetNotReady");
            var items = this.__form.getItems();
            // {
            //      "image":{"id":561,"options":{"restrict":"false","width":"693","skipSmall":"false","resize":"true"}},
            //       "link":"page:e96b3224e0ef7850e6c86d6d857b327b | Главная","description":"test"
            // }
            if (items["image"] && model["image"] != null) {
                items["image"].setModel(model["image"]);
            }
            if (this.__bf) {
                if (model["link"] != null) {
                    this.__bf.setValue(model["link"]);
                } else {
                    this.__bf.resetValue();
                }
            }
            if (items["description"]) {
                if (model["description"] != null) {
                    items["description"].setValue(model["description"]);
                } else {
                    items["description"].resetValue();
                }
            }
            if (items["customName"]) {
                if (model["name"] != null) {
                    items["customName"].setValue(model["name"]);
                } else {
                    items["customName"].resetValue();
                }
            }
            if (items["styles"] && model["style"] != null) {
                items["styles"].setModelSelection([model["style"]]);
            }
            if (items["styles2"] && model["style2"] != null) {
                items["styles2"].setModelSelection([model["style2"]]);
            }
            if (items["styles3"] && model["style3"] != null) {
                items["styles3"].setModelSelection([model["style3"]]);
            }

            this.removeState("widgetNotReady");
        },

        valueAsJSON: function () {
            if (!this.__form.validate()) {
                return null;
            }
            var data = {};
            if (this.__imageAM) {
                data["image"] = this.__imageAM.valueAsJSON();
            }
            //qx.log.Logger.info("data.image=" + JSON.stringify(data["image"]));
            var items = this.__form.getItems();
            var link = data["link"] = (items["link"] != null) ? items["link"].getValue() : null;
            if (items["description"]) {
                data["description"] = items["description"].getValue();
            }

            if (items["customName"]) {
                data["name"] = items["customName"].getValue();
            } else {
                data["name"] = link && link.split("|")[1];
                if (data["name"]) {
                    data["name"] = data["name"].trim();
                    if (items["styles"]) {
                        var item = items["styles"].getSelection()[0];
                        if (item != null) {
                            data["name"] = data["name"] + " (" + String(item.getLabel()) + ")";
                        }
                    }
                } else if (data["image"] != null) {
                    data["name"] = data["image"]["path"];
                }
            }

            if (items["styles"]) {
                data["style"] = items["styles"].getModelSelection().getItem(0);
            }
            if (items["styles2"]) {
                data["style2"] = items["styles2"].getModelSelection().getItem(0);
            }
            if (items["styles3"]) {
                data["style3"] = items["styles3"].getModelSelection().getItem(0);
            }
            return data;
        }
    },

    destruct: function () {
        this.__bf = null;
        this.__asmSpec = null;
        this.__attrSpec = null;
        this._disposeObjects("__form", "__imageAM");
    }
});
