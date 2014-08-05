/**
 * Edit page tab of page editor tabbox.
 *
 * @asset(ncms/icon/16/misc/document-template.png)
 * @asset(ncms/icon/16/misc/tick.png)
 * @asset(ncms/icon/16/misc/cross-script.png)
 * @asset(ncms/icon/16/misc/monitor.png)
 */
qx.Class.define("ncms.pgs.PageEditorEditPage", {
    extend : qx.ui.tabview.Page,
    include : [ ncms.pgs.MPageEditorPane ],


    properties : {

        /**
         * Extended page spec used in this page tab.
         */
        "pageEditSpec" : {
            "check" : "Object",
            "apply" : "__applyPageEditSpec"
        }
    },

    construct : function() {
        this.base(arguments, this.tr("Edit"));
        this.setLayout(new qx.ui.layout.VBox(1));

        var header = new qx.ui.container.Composite(new qx.ui.layout.VBox(5))
                .set({backgroundColor : "#EBEBEB", padding : [5, 5, 10, 5]});

        //Page name
        this.__pageNameLabel = new qx.ui.basic.Label();
        this.__pageNameLabel.setFont("headline");
        header.add(this.__pageNameLabel);

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));

        this.__templateBf =
                new sm.ui.form.ButtonField(this.tr("Template"),
                        "ncms/icon/16/misc/document-template.png",
                        true).set({readOnly : true});
        this.__templateBf.setPlaceholder(this.tr("Please select the page template"));
        this.__templateBf.addListener("execute", this.__onChangeTemplate, this);
        hcont.add(this.__templateBf, {flex : 1});

        var bt = this.__previewBt = new qx.ui.form.Button(this.tr("Preview"), "ncms/icon/16/misc/monitor.png");
        bt.addListener("execute", this.__preview, this);
        hcont.add(bt);

        bt = this.__saveBt = new qx.ui.form.Button(this.tr("Save"), "ncms/icon/16/misc/tick.png");
        bt.setEnabled(false);
        bt.addListener("execute", this.__save, this);
        hcont.add(bt);

        bt = this.__cancelBt = new qx.ui.form.Button(this.tr("Cancel"), "ncms/icon/16/misc/cross-script.png");
        bt.setEnabled(false);
        bt.addListener("execute", this.__cancel, this);
        hcont.add(bt);

        header.add(hcont);
        this.add(header);

        this.addListener("loadPane", this.__onLoadPane, this);
    },

    members : {

        /**
         * Page template selector
         */
        __templateBf : null,

        /**
         * Page controls container
         */
        __scroll : null,

        /**
         * Current page form
         */
        __form : null,

        __saveBt : null,

        __cancelBt : null,

        __previewBt : null,

        __onLoadPane : function(ev) {
            var spec = ev.getData();
            this.__pageNameLabel.setValue(spec["name"]);
            //{"id":4,"name":"test"}
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.edit", spec),
                    "GET", "application/json");
            req.send(function(resp) {
                this.setPageEditSpec(resp.getContent());
            }, this);
        },

        __applyPageEditSpec : function(spec) {
            var t = spec["template"];
            if (t == null) {
                this.__templateBf.resetValue();
                this.__cleanupFormPane();
                this.__syncState();
                return;
            }
            var sb = [];
            if (t["description"] != null) {
                sb.push(t["description"]);
            }
            if (t["name"] != null) {
                sb.push(t["name"]);
            }
            this.__templateBf.setValue(sb.join(" | "));

            var attrs = spec["attributes"] || [];
            var form = new sm.ui.form.ExtendedForm();
            var vmgr = form.getValidationManager();
            vmgr.setRequiredFieldMessage(this.tr("This field is required"));

            attrs.forEach(function(attrSpec) {
                this.__processAttribute(attrSpec, spec, form);
            }, this);

            this.__cleanupFormPane();
            this.__form = form;
            this.__scroll = new qx.ui.container.Scroll().set({marginTop : 5});
            //this.__scroll.add(new sm.ui.form.FlexFormRenderer(form));
            this.__scroll.add(new sm.ui.form.OneColumnFormRenderer(form));
            this.add(this.__scroll, {flex : 1});

            this.__syncState();
            this.setModified(false);
        },


        __cleanupFormPane : function() {
            if (this.__scroll) {
                this.__scroll.destroy();
                this.__scroll = null;
            }
            if (this.__form != null) {
                var items = this.__form.getItems();
                for (var k in items) {
                    var w = items[k];
                    var am = w.getUserData("attributeManager");
                    w.setUserData("attributeManager", null);
                    if (am) {
                        try {
                            am.dispose();
                        } catch (e) {
                            qx.log.Logger.error(e);
                        }
                    }
                    try {
                        w.destroy();
                    } catch (e) {
                        qx.log.Logger.error(e);
                    }
                }
                this.__form.dispose();
                this.__form = null;
            }
        },

        __processAttribute : function(attrSpec, asmSpec, form) {
            var am = ncms.asm.am.AsmAttrManagersRegistry.createAttrManagerInstanceForType(attrSpec["type"]);
            if (am == null) {
                qx.log.Logger.warn("Missing attribute manager for type: " + attrSpec["type"]);
                return;
            }
            var w = am.activateValueEditorWidget(attrSpec, asmSpec);
            if (w == null) {
                qx.log.Logger.warn("Attribute manager used for type: " + attrSpec["type"] + " produced invalid widget: null");
                return;
            }
            var wclass = qx.Class.getByName(w.classname);
            if (wclass == null) {
                qx.log.Logger.warn("Attribute manager used for type: " + attrSpec["type"] + " produced invalid widget: " + w);
                return;
            }

            var oou = qx.util.OOUtil;

            var awclass = wclass;
            var aw = w;
            if (w.getUserData("ncms.asm.activeWidget") != null) {
                aw = w.getUserData("ncms.asm.activeWidget");
                awclass = qx.Class.getByName(aw.classname);
                if (awclass == null) {
                    qx.log.Logger.warn("Attribute manager used for type: " + attrSpec["type"] +
                            " produced invalid 'ncms.asm.activeWidget' widget: " + aw);
                    return;
                }
                w.setUserData("ncms.asm.activeWidget", null);
            }

            //Listen modified events
            if (qx.Class.hasInterface(awclass, ncms.asm.am.IValueWidget)) {
                aw.addListener("modified", this._onModifiedWidget, this);
            } else if (oou.supportsEvent(awclass, "input") && !((typeof aw.getReadOnly === "function") && aw.getReadOnly() === true)) {
                aw.addListener("input", this._onModifiedWidget, this);
            } else if (oou.supportsEvent(awclass, "changeValue")) {
                aw.addListener("changeValue", this._onModifiedWidget, this);
            } else if (oou.supportsEvent(awclass, "changeSelection")) {
                aw.addListener("changeSelection", this._onModifiedWidget, this);
            } else if (oou.supportsEvent(awclass, "execute")) {
                aw.addListener("execute", this._onModifiedWidget, this);
            }

            if (!qx.Class.hasInterface(wclass, qx.ui.form.IForm)) {
                w = new sm.ui.form.FormWidgetAdapter(w);
            }

            w.setUserData("attributeManager", am);
            var validator = null;
            if (typeof w.getUserData("ncms.asm.validator") === "function") {
                validator = w.getUserData("ncms.asm.validator");
            } else if (typeof w.getValidator === "function") {
                validator = w.getValidator();
            }

            form.add(w, attrSpec["label"], validator, attrSpec["name"], w);
        },

        _onModifiedWidget : function(ev) {
            var w = ev.getTarget();
            if (w != null) {
                if (w.getEnabled() === false || w.hasState("widgetNotReady")) {
                    return;
                }
            }
            this.setModified(true);
        },

        _applyModified : function(val) {
            this.__saveBt.setEnabled(val);
            this.__cancelBt.setEnabled(val);
        },

        __onChangeTemplate : function() {
            var pspec = this.getPageSpec();
            var dlg = new ncms.asm.AsmSelectorDlg(this.tr("Please select the template page"), null, {
                "template" : true
            }, ["description", "name"]);
            dlg.addListener("completed", function(ev) {
                var t = ev.getData()[0];
                var url = ncms.Application.ACT.getRestUrl("pages.set.template", {
                    id : pspec["id"],
                    templateId : t["id"]
                });
                var req = new sm.io.Request(url, "PUT", "application/json");
                req.send(function(resp) {
                    this.setPageEditSpec(resp.getContent());
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        },

        __syncState : function() {
            this.__previewBt.setEnabled(this.__form != null);
        },

        __save : function() {
            if (this.__form == null || !this.__form.validate()) {
                ncms.Application.infoPopup(this.tr("Page fields contain errors"), {
                    showTime : 5000,
                    icon : "ncms/icon/32/exclamation.png"
                });
                return;
            }
            this.__saveBt.setEnabled(false);
            try {
                var data = {};
                var items = this.__form.getItems();
                for (var k in items) {
                    var w = items[k];
                    var am = w.getUserData("attributeManager");
                    if (am == null) {
                        continue;
                    }
                    data[k] = am.valueAsJSON();
                }
                var spec = this.getPageSpec();
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.edit", spec), "PUT");
                req.setRequestContentType("application/json");
                req.setData(JSON.stringify(data));
                req.addListener("error", function() {
                    this.__saveBt.setEnabled(true);
                }, this);
                req.send(function(resp) {
                    this.setModified(false);
                    ncms.Application.infoPopup(this.tr("Page '%1' saved successfully", spec["name"]));
                }, this);
            } catch(e) {
                this.__saveBt.setEnabled(true);
            }
        },

        __cancel : function() {
            ncms.Application.confirm(this.tr("Dow you really want to dispose pending changes?"), function(yes) {
                if (yes) {
                    this.setPageSpec(sm.lang.Object.shallowClone(this.getPageSpec()));
                }
            }, this);
        },

        __preview : function() {
            //todo
        }
    },

    destruct : function() {
        this.__cleanupFormPane();
        this.__templateBf = null;
        this.__form = null;
        this.__scroll = null;
        this.__previewBt = null;
        this.__saveBt = null;
        this.__cancelBt = null;
    }
});